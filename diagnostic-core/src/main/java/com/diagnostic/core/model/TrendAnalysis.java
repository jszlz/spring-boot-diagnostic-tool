package com.diagnostic.core.model;

public class TrendAnalysis {
    private String endpoint;
    private TrendDirection direction;
    private double changeRate;
    private boolean degrading;

    public TrendAnalysis() {
    }

    public TrendAnalysis(String endpoint, TrendDirection direction, double changeRate, boolean degrading) {
        this.endpoint = endpoint;
        this.direction = direction;
        this.changeRate = changeRate;
        this.degrading = degrading;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public TrendDirection getDirection() {
        return direction;
    }

    public void setDirection(TrendDirection direction) {
        this.direction = direction;
    }

    public double getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(double changeRate) {
        this.changeRate = changeRate;
    }

    public boolean isDegrading() {
        return degrading;
    }

    public void setDegrading(boolean degrading) {
        this.degrading = degrading;
    }

    public enum TrendDirection {
        RISING,
        FALLING,
        STABLE
    }
}
