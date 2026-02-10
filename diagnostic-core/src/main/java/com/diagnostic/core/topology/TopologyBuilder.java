package com.diagnostic.core.topology;

import com.diagnostic.core.model.*;
import com.diagnostic.core.registry.DependencyRegistry;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Builder for dependency topology graphs.
 * Uses JGraphT to construct directed graphs representing application dependencies,
 * and detects circular dependencies.
 */
public class TopologyBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TopologyBuilder.class);

    private final DependencyRegistry dependencyRegistry;

    public TopologyBuilder(DependencyRegistry dependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry;
    }

    /**
     * Build the complete dependency topology graph.
     *
     * @return dependency topology with graph and cyclic nodes
     */
    public DependencyTopology buildTopology() {
        Graph<TopologyNode, TopologyEdge> graph = new DefaultDirectedGraph<>(TopologyEdge.class);

        // Add application node as the root
        TopologyNode appNode = new TopologyNode("application", NodeType.APPLICATION);
        appNode.getProperties().put("name", "Application");
        graph.addVertex(appNode);

        // Add all external dependencies as nodes
        Collection<ExternalDependency> dependencies = dependencyRegistry.getAllDependencies();
        Map<String, TopologyNode> nodeMap = new HashMap<>();

        for (ExternalDependency dep : dependencies) {
            TopologyNode depNode = createNodeFromDependency(dep);
            graph.addVertex(depNode);
            nodeMap.put(dep.getId(), depNode);

            // Add edge from application to dependency
            TopologyEdge edge = new TopologyEdge(dep.getCallCount());
            graph.addEdge(appNode, depNode, edge);
        }

        // Detect circular dependencies
        Set<TopologyNode> cyclicNodes = detectCycles(graph);

        // Build topology object
        DependencyTopology topology = new DependencyTopology(graph, cyclicNodes);
        
        // Populate graph data for JSON serialization
        topology.setGraphData(buildGraphData(graph, cyclicNodes));

        logger.info("Built topology with {} nodes and {} edges, {} cyclic nodes",
                   graph.vertexSet().size(),
                   graph.edgeSet().size(),
                   cyclicNodes.size());

        return topology;
    }

    /**
     * Create a topology node from an external dependency.
     *
     * @param dependency the external dependency
     * @return topology node
     */
    private TopologyNode createNodeFromDependency(ExternalDependency dependency) {
        NodeType nodeType = mapDependencyTypeToNodeType(dependency.getType());
        TopologyNode node = new TopologyNode(dependency.getId(), nodeType);

        // Copy metadata
        node.getProperties().putAll(dependency.getMetadata());
        node.getProperties().put("critical", dependency.isCritical());
        node.getProperties().put("hasRedundancy", dependency.hasRedundancy());
        node.getProperties().put("callCount", dependency.getCallCount());
        
        if (dependency.getConnectionString() != null) {
            node.getProperties().put("connectionString", dependency.getConnectionString());
        }

        return node;
    }

    /**
     * Map dependency type to node type.
     *
     * @param dependencyType the dependency type
     * @return corresponding node type
     */
    private NodeType mapDependencyTypeToNodeType(DependencyType dependencyType) {
        switch (dependencyType) {
            case DATABASE:
                return NodeType.DATABASE;
            case REDIS:
                return NodeType.REDIS;
            case HTTP_SERVICE:
                return NodeType.HTTP_SERVICE;
            case MESSAGE_QUEUE:
                return NodeType.MESSAGE_QUEUE;
            default:
                return NodeType.APPLICATION;
        }
    }

    /**
     * Detect circular dependencies in the graph.
     *
     * @param graph the dependency graph
     * @return set of nodes involved in cycles
     */
    private Set<TopologyNode> detectCycles(Graph<TopologyNode, TopologyEdge> graph) {
        try {
            CycleDetector<TopologyNode, TopologyEdge> cycleDetector = new CycleDetector<>(graph);
            Set<TopologyNode> cyclicNodes = cycleDetector.findCycles();
            
            if (!cyclicNodes.isEmpty()) {
                logger.warn("Detected {} nodes involved in circular dependencies", cyclicNodes.size());
                cyclicNodes.forEach(node -> 
                    logger.warn("Cyclic node: {} ({})", node.getId(), node.getType())
                );
            }
            
            return cyclicNodes;
        } catch (Exception e) {
            logger.error("Error detecting cycles: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Build graph data structure for JSON serialization.
     *
     * @param graph the JGraphT graph
     * @param cyclicNodes nodes involved in cycles
     * @return map containing nodes and edges data
     */
    private Map<String, Object> buildGraphData(Graph<TopologyNode, TopologyEdge> graph,
                                               Set<TopologyNode> cyclicNodes) {
        Map<String, Object> graphData = new HashMap<>();

        // Build nodes list
        List<Map<String, Object>> nodesList = new ArrayList<>();
        for (TopologyNode node : graph.vertexSet()) {
            Map<String, Object> nodeData = new HashMap<>();
            nodeData.put("id", node.getId());
            nodeData.put("type", node.getType().toString());
            nodeData.put("properties", node.getProperties());
            nodeData.put("cyclic", cyclicNodes.contains(node));
            nodesList.add(nodeData);
        }

        // Build edges list
        List<Map<String, Object>> edgesList = new ArrayList<>();
        for (TopologyEdge edge : graph.edgeSet()) {
            TopologyNode source = graph.getEdgeSource(edge);
            TopologyNode target = graph.getEdgeTarget(edge);

            Map<String, Object> edgeData = new HashMap<>();
            edgeData.put("source", source.getId());
            edgeData.put("target", target.getId());
            edgeData.put("callCount", edge.getCallCount());
            edgeData.put("averageLatency", edge.getAverageLatency());
            edgesList.add(edgeData);
        }

        graphData.put("nodes", nodesList);
        graphData.put("edges", edgesList);
        graphData.put("cyclicNodeCount", cyclicNodes.size());

        return graphData;
    }

    /**
     * Export topology to JSON format.
     *
     * @param topology the topology to export
     * @return JSON string representation
     */
    public String exportToJson(DependencyTopology topology) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = 
                new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(topology.getGraphData());
        } catch (Exception e) {
            logger.error("Error exporting topology to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Export topology to DOT format for Graphviz.
     *
     * @param topology the topology to export
     * @return DOT format string
     */
    public String exportToDot(DependencyTopology topology) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph DependencyTopology {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=box, style=rounded];\n\n");

        Graph<TopologyNode, TopologyEdge> graph = topology.getGraph();
        Set<TopologyNode> cyclicNodes = topology.getCyclicNodes();

        // Add nodes
        for (TopologyNode node : graph.vertexSet()) {
            String nodeId = sanitizeForDot(node.getId());
            String label = node.getId();
            String color = getNodeColor(node.getType());
            String style = cyclicNodes.contains(node) ? "filled,bold" : "filled";
            String fillColor = cyclicNodes.contains(node) ? "lightcoral" : color;

            dot.append(String.format("  \"%s\" [label=\"%s\", fillcolor=\"%s\", style=\"%s\"];\n",
                                    nodeId, label, fillColor, style));
        }

        dot.append("\n");

        // Add edges
        for (TopologyEdge edge : graph.edgeSet()) {
            TopologyNode source = graph.getEdgeSource(edge);
            TopologyNode target = graph.getEdgeTarget(edge);
            String sourceId = sanitizeForDot(source.getId());
            String targetId = sanitizeForDot(target.getId());
            String label = String.format("calls: %d", edge.getCallCount());

            dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\"];\n",
                                    sourceId, targetId, label));
        }

        dot.append("}\n");

        return dot.toString();
    }

    /**
     * Get color for node based on type.
     *
     * @param nodeType the node type
     * @return color name
     */
    private String getNodeColor(NodeType nodeType) {
        switch (nodeType) {
            case APPLICATION:
                return "lightblue";
            case DATABASE:
                return "lightgreen";
            case REDIS:
                return "lightyellow";
            case HTTP_SERVICE:
                return "lightpink";
            case MESSAGE_QUEUE:
                return "lavender";
            default:
                return "white";
        }
    }

    /**
     * Sanitize string for DOT format.
     *
     * @param str the string to sanitize
     * @return sanitized string
     */
    private String sanitizeForDot(String str) {
        return str.replaceAll("\"", "\\\\\"");
    }
}
