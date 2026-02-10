package com.diagnostic.core.model;

import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependencyTopology {
    private Graph<TopologyNode, TopologyEdge> graph;
    private Set<TopologyNode> cyclicNodes;
    private Map<String, Object> graphData;

    public DependencyTopology() {
        this.cyclicNodes = new HashSet<>();
        this.graphData = new HashMap<>();
    }

    public DependencyTopology(Graph<TopologyNode, TopologyEdge> graph, Set<TopologyNode> cyclicNodes) {
        this.graph = graph;
        this.cyclicNodes = cyclicNodes != null ? cyclicNodes : new HashSet<>();
        this.graphData = new HashMap<>();
    }

    public Graph<TopologyNode, TopologyEdge> getGraph() {
        return graph;
    }

    public void setGraph(Graph<TopologyNode, TopologyEdge> graph) {
        this.graph = graph;
    }

    public Set<TopologyNode> getCyclicNodes() {
        return cyclicNodes;
    }

    public void setCyclicNodes(Set<TopologyNode> cyclicNodes) {
        this.cyclicNodes = cyclicNodes;
    }

    public Map<String, Object> getGraphData() {
        return graphData;
    }

    public void setGraphData(Map<String, Object> graphData) {
        this.graphData = graphData;
    }
}
