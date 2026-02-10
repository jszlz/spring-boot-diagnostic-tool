package com.diagnostic.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TopologyNode {
    private String id;
    private NodeType type;
    private Map<String, Object> properties;

    public TopologyNode() {
        this.properties = new HashMap<>();
    }

    public TopologyNode(String id, NodeType type) {
        this.id = id;
        this.type = type;
        this.properties = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopologyNode that = (TopologyNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TopologyNode{" +
                "id='" + id + '\'' +
                ", type=" + type +
                '}';
    }
}
