package com.diagnostic.core.model;

import java.util.Objects;

public class EndpointMetrics {
    private String endpoint;
    private long duration; // in nanoseconds
    private int statusCode;
    private long timestamp;
    private String method; // GET, POST, etc.
    private String controllerClass;
    private String controllerMethod;

    public EndpointMetrics() {
    }

    private EndpointMetrics(Builder builder) {
        this.endpoint = builder.endpoint;
        this.duration = builder.duration;
        this.statusCode = builder.statusCode;
        this.timestamp = builder.timestamp;
        this.method = builder.method;
        this.controllerClass = builder.controllerClass;
        this.controllerMethod = builder.controllerMethod;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    public String getControllerMethod() {
        return controllerMethod;
    }

    public void setControllerMethod(String controllerMethod) {
        this.controllerMethod = controllerMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointMetrics that = (EndpointMetrics) o;
        return duration == that.duration &&
                statusCode == that.statusCode &&
                timestamp == that.timestamp &&
                Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, duration, statusCode, timestamp);
    }

    public static class Builder {
        private String endpoint;
        private long duration;
        private int statusCode;
        private long timestamp;
        private String method;
        private String controllerClass;
        private String controllerMethod;

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder controllerClass(String controllerClass) {
            this.controllerClass = controllerClass;
            return this;
        }

        public Builder controllerMethod(String controllerMethod) {
            this.controllerMethod = controllerMethod;
            return this;
        }

        public EndpointMetrics build() {
            return new EndpointMetrics(this);
        }
    }
}
