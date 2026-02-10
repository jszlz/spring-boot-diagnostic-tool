package com.diagnostic.core.registry;

import com.diagnostic.core.model.ExternalDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for managing external dependencies discovered in the application.
 * This component stores all detected dependencies (databases, Redis, HTTP services, message queues)
 * and provides methods to query them.
 */
public class DependencyRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DependencyRegistry.class);

    private final Map<String, ExternalDependency> dependencies = new ConcurrentHashMap<>();

    /**
     * Register a new external dependency.
     * If a dependency with the same ID already exists, it will be replaced.
     *
     * @param dependency the dependency to register
     */
    public void register(ExternalDependency dependency) {
        if (dependency == null || dependency.getId() == null) {
            logger.warn("Attempted to register null dependency or dependency with null ID");
            return;
        }

        ExternalDependency existing = dependencies.put(dependency.getId(), dependency);
        if (existing != null) {
            logger.debug("Replaced existing dependency: {}", dependency.getId());
        } else {
            logger.info("Registered new dependency: {} (type: {})", dependency.getId(), dependency.getType());
        }
    }

    /**
     * Get all registered dependencies.
     *
     * @return collection of all dependencies
     */
    public Collection<ExternalDependency> getAllDependencies() {
        return dependencies.values();
    }

    /**
     * Get a specific dependency by ID.
     *
     * @param id the dependency ID
     * @return the dependency, or null if not found
     */
    public ExternalDependency getDependency(String id) {
        return dependencies.get(id);
    }

    /**
     * Check if a dependency with the given ID exists.
     *
     * @param id the dependency ID
     * @return true if the dependency exists, false otherwise
     */
    public boolean hasDependency(String id) {
        return dependencies.containsKey(id);
    }

    /**
     * Remove a dependency by ID.
     *
     * @param id the dependency ID
     * @return the removed dependency, or null if not found
     */
    public ExternalDependency removeDependency(String id) {
        ExternalDependency removed = dependencies.remove(id);
        if (removed != null) {
            logger.info("Removed dependency: {}", id);
        }
        return removed;
    }

    /**
     * Clear all registered dependencies.
     */
    public void clear() {
        int count = dependencies.size();
        dependencies.clear();
        logger.info("Cleared {} dependencies from registry", count);
    }

    /**
     * Get the total number of registered dependencies.
     *
     * @return the count of dependencies
     */
    public int size() {
        return dependencies.size();
    }
}
