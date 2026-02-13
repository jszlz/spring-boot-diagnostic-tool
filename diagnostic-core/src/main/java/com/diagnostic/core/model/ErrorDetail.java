package com.diagnostic.core.model;

/**
 * Error detail information for a specific request.
 */
public class ErrorDetail {
    private long timestamp;
    private int statusCode;
    private String method;
    private String endpoint;
    private long duration; // in milliseconds
    
    public ErrorDetail() {
    }
    
    public ErrorDetail(long timestamp, int statusCode, String method, String endpoint, long duration) {
        this.timestamp = timestamp;
        this.statusCode = statusCode;
        this.method = method;
        this.endpoint = endpoint;
        this.duration = duration;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
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
}
