package com.diagnostic.core.model;

import java.util.ArrayList;
import java.util.List;

public class ArchitectureRisk {
    private RiskType type;
    private Severity severity;
    private String component;
    private String description;
    private List<String> recommendations;

    public ArchitectureRisk() {
        this.recommendations = new ArrayList<>();
    }

    private ArchitectureRisk(Builder builder) {
        this.type = builder.type;
        this.severity = builder.severity;
        this.component = builder.component;
        this.description = builder.description;
        this.recommendations = builder.recommendations;
    }

    public static Builder builder() {
        return new Builder();
    }

    public RiskType getType() {
        return type;
    }

    public void setType(RiskType type) {
        this.type = type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public static class Builder {
        private RiskType type;
        private Severity severity;
        private String component;
        private String description;
        private List<String> recommendations = new ArrayList<>();

        public Builder type(RiskType type) {
            this.type = type;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder component(String component) {
            this.component = component;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder recommendations(List<String> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public Builder addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
            return this;
        }

        public ArchitectureRisk build() {
            return new ArchitectureRisk(this);
        }
    }
}
