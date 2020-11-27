/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.jcr.resource.internal;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.jackrabbit.api.observation.JackrabbitEventFilter;
import org.apache.jackrabbit.api.observation.JackrabbitObservationManager;
import org.apache.jackrabbit.oak.jcr.observation.filter.FilterFactory;
import org.apache.jackrabbit.oak.jcr.observation.filter.OakEventFilter;
import org.apache.sling.api.resource.observation.ResourceChange.ChangeType;
import org.apache.sling.api.resource.path.Path;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.spi.resource.provider.ObservationReporter;
import org.apache.sling.spi.resource.provider.ObserverConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base configuration for a JCR listener, shared
 * by all registered {@link JcrResourceListener}s.
 */
public class JcrListenerBaseConfig implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(JcrResourceListener.class);

    private final Session session;

    private final ObservationReporter reporter;

    @SuppressWarnings("deprecation")
    public JcrListenerBaseConfig(
                    final ObservationReporter reporter,
                    final SlingRepository repository)
    throws RepositoryException {
        this.reporter = reporter;
        // The session should have read access on the whole repository
        this.session = repository.loginService("observation", repository.getDefaultWorkspace());
    }

    /**
     * Dispose this config
     * Close session.
     */
    @Override
    public void close() throws IOException {
        this.session.logout();
    }

    /**
     * Register a JCR event listener
     * @param listener The listener
     * @param config The configuration
     * @throws RepositoryException If registration fails.
     */
/**
 * Register a JCR event listener
 *
 * @param listener
 * 		The listener
 * @param config
 * 		The configuration
 * @throws RepositoryException
 * 		If registration fails.
 */
public void register(final javax.jcr.observation.EventListener listener, final org.apache.sling.spi.resource.provider.ObserverConfiguration config) throws javax.jcr.RepositoryException {
    final javax.jcr.observation.ObservationManager mgr = this.session.getWorkspace().getObservationManager();
    if (mgr instanceof org.apache.jackrabbit.api.observation.JackrabbitObservationManager) {
        final org.apache.jackrabbit.oak.jcr.observation.filter.OakEventFilter filter = org.apache.jackrabbit.oak.jcr.observation.filter.FilterFactory.wrap(new org.apache.jackrabbit.api.observation.JackrabbitEventFilter());
        // paths
        final java.util.Set<java.lang.String> paths = config.getPaths().toStringSet();
        int globCount = 0;
        int pathCount = 0;
        for (final java.lang.String p : paths) {
            if (p.startsWith(org.apache.sling.api.resource.path.Path.GLOB_PREFIX)) {
                globCount++;
            } else {
                pathCount++;
            }
        }
        final java.lang.String[] pathArray = (pathCount > 0) ? new java.lang.String[pathCount] : null;
        final java.lang.String[] globArray = (globCount > 0) ? new java.lang.String[globCount] : null;
        pathCount = 0;
        globCount = 0;
        // create arrays and remove global prefix
        for (final java.lang.String p : paths) {
            if (p.startsWith(org.apache.sling.api.resource.path.Path.GLOB_PREFIX)) {
                globArray[globCount] = p.substring(org.apache.sling.api.resource.path.Path.GLOB_PREFIX.length());
                globCount++;
            } else {
                pathArray[pathCount] = p;
                pathCount++;
            }
        }
        if (globArray != null) {
            filter.withIncludeGlobPaths(globArray);
        }
        {
            filter.setAdditionalPaths(/* NPEX_NULL_EXP */
            pathArray);
        }
        filter.setIsDeep(true);
        // exclude paths
        final java.util.Set<java.lang.String> excludePaths = config.getExcludedPaths().toStringSet();
        if (!excludePaths.isEmpty()) {
            filter.setExcludedPaths(excludePaths.toArray(new java.lang.String[excludePaths.size()]));
        }
        // external
        filter.setNoExternal(!config.includeExternal());
        // types
        filter.setEventTypes(this.getTypes(config));
        // nt:file handling
        filter.withNodeTypeAggregate(new java.lang.String[]{ "nt:file" }, new java.lang.String[]{ "", "jcr:content" });
        // ancestors remove
        filter.withIncludeAncestorsRemove();
        ((org.apache.jackrabbit.api.observation.JackrabbitObservationManager) (mgr)).addEventListener(listener, filter);
    } else {
        throw new javax.jcr.RepositoryException("Observation manager is not a JackrabbitObservationManager");
    }
}

    /**
     * Get the event types based on the configuration
     * @param c The configuration
     * @return The event type mask
     */
    private int getTypes(final ObserverConfiguration c) {
        int result = 0;
        for (ChangeType t : c.getChangeTypes()) {
            switch (t) {
            case ADDED:
                result = result | Event.NODE_ADDED;
                break;
            case REMOVED:
                result = result | Event.NODE_REMOVED;
                break;
            case CHANGED:
                result = result | Event.PROPERTY_ADDED;
                result = result | Event.PROPERTY_CHANGED;
                result = result | Event.PROPERTY_REMOVED;
                break;
            default:
                break;
            }
        }
        return result;
    }

    /**
     * Unregister the listener.
     * @param listener The listener
     */
    public void unregister(final EventListener listener) {
        try {
            this.session.getWorkspace().getObservationManager().removeEventListener(listener);
        } catch (final RepositoryException e) {
            logger.warn("Unable to remove session listener: " + this, e);
        }
    }

    /**
     * The observation reporter
     * @return The observation reporter.
     */
    public ObservationReporter getReporter() {
        return this.reporter;
    }
}
