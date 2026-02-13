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
    private EndpointType endpointType; // BUSINESS or DIAGNOSTIC
    private String clientIp;        // 客户端IP地址
    private String userAgent;       // 用户代理

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
        this.endpointType = builder.endpointType;
        this.clientIp = builder.clientIp;
        this.userAgent = builder.userAgent;
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

    public EndpointType getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
        private EndpointType endpointType;
        private String clientIp;
        private String userAgent;

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

        public Builder endpointType(EndpointType endpointType) {
            this.endpointType = endpointType;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public EndpointMetrics build() {
            return new EndpointMetrics(this);
        }
    }
}
