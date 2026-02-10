package com.diagnostic.core.model;

public class TopologyEdge {
    private long callCount;
    private double averageLatency;

    public TopologyEdge() {
    }

    public TopologyEdge(long callCount) {
        this.callCount = callCount;
    }

    public TopologyEdge(long callCount, double averageLatency) {
        this.callCount = callCount;
        this.averageLatency = averageLatency;
    }

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public void setAverageLatency(double averageLatency) {
        this.averageLatency = averageLatency;
    }

    @Override
    public String toString() {
        return "TopologyEdge{" +
                "callCount=" + callCount +
                ", averageLatency=" + averageLatency +
                '}';
    }
}
