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
package org.apache.geronimo.jcache.simple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

public class ConfigurableMBeanServerIdBuilder extends MBeanServerBuilder {

    private static ConcurrentMap<Key, MBeanServer> JVM_SINGLETONS = new ConcurrentHashMap<>();

    @Override
public javax.management.MBeanServer newMBeanServer(final java.lang.String defaultDomain, final javax.management.MBeanServer outer, final javax.management.MBeanServerDelegate delegate) {
    final org.apache.geronimo.jcache.simple.ConfigurableMBeanServerIdBuilder.Key key = new org.apache.geronimo.jcache.simple.ConfigurableMBeanServerIdBuilder.Key(defaultDomain, outer);
    javax.management.MBeanServer server = org.apache.geronimo.jcache.simple.ConfigurableMBeanServerIdBuilder.JVM_SINGLETONS.get(key);
    if (server == null) {
        server = super.newMBeanServer(defaultDomain, outer, new org.apache.geronimo.jcache.simple.ConfigurableMBeanServerIdBuilder.ForceIdMBeanServerDelegate(delegate));
        final javax.management.MBeanServer existing = org.apache.geronimo.jcache.simple.ConfigurableMBeanServerIdBuilder.JVM_SINGLETONS.putIfAbsent(key, server);
        {
            server = /* NPEX_NULL_EXP */
            existing;
        }
    }
    return server;
}

    private static class Key {

        private final String domain;

        private final MBeanServer outer;

        private Key(final String domain, final MBeanServer outer) {
            this.domain = domain;
            this.outer = outer;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final Key key = Key.class.cast(o);
            return !(domain != null ? !domain.equals(key.domain) : key.domain != null)
                    && !(outer != null ? !outer.equals(key.outer) : key.outer != null);

        }

        @Override
        public int hashCode() {
            int result = domain != null ? domain.hashCode() : 0;
            result = 31 * result + (outer != null ? outer.hashCode() : 0);
            return result;
        }
    }

    private class ForceIdMBeanServerDelegate extends MBeanServerDelegate {

        private final MBeanServerDelegate delegate;

        public ForceIdMBeanServerDelegate(final MBeanServerDelegate delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getMBeanServerId() {
            return System.getProperty("org.jsr107.tck.management.agentId", delegate.getMBeanServerId());
        }

        @Override
        public String getSpecificationName() {
            return delegate.getSpecificationName();
        }

        @Override
        public String getSpecificationVersion() {
            return delegate.getSpecificationVersion();
        }

        @Override
        public String getSpecificationVendor() {
            return delegate.getSpecificationVendor();
        }

        @Override
        public String getImplementationName() {
            return delegate.getImplementationName();
        }

        @Override
        public String getImplementationVersion() {
            return delegate.getImplementationVersion();
        }

        @Override
        public String getImplementationVendor() {
            return delegate.getImplementationVendor();
        }

        @Override
        public MBeanNotificationInfo[] getNotificationInfo() {
            return delegate.getNotificationInfo();
        }

        @Override
        public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter,
                final Object handback) throws IllegalArgumentException {
            delegate.addNotificationListener(listener, filter, handback);
        }

        @Override
        public void removeNotificationListener(final NotificationListener listener, final NotificationFilter filter,
                final Object handback) throws ListenerNotFoundException {
            delegate.removeNotificationListener(listener, filter, handback);
        }

        @Override
        public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
            delegate.removeNotificationListener(listener);
        }

        @Override
        public void sendNotification(final Notification notification) {
            delegate.sendNotification(notification);
        }
    }
}