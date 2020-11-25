/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.validation.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.sling.validation.model.ChildResource;
import org.apache.sling.validation.model.ResourceProperty;
import org.apache.sling.validation.model.ValidationModel;
import org.jetbrains.annotations.NotNull;

/**
 * Builder to instantiate a {@link ValidationModel}
 *
 */
public class ValidationModelBuilder {

    private final @NotNull List<ResourceProperty> resourceProperties;
    private final @NotNull List<ChildResource> children;
    private final @NotNull Collection<String> applicablePaths;
    
    public ValidationModelBuilder() {
        resourceProperties = new ArrayList<ResourceProperty>();
        children = new ArrayList<>();
        applicablePaths = new ArrayList<>();
    }
    
    public @NotNull ValidationModelBuilder resourceProperty(@NotNull ResourceProperty resourceProperty) {
        resourceProperties.add(resourceProperty);
        return this;
    }
    
    public @NotNull ValidationModelBuilder resourceProperties(@NotNull List<ResourceProperty> resourceProperties) {
        this.resourceProperties.addAll(resourceProperties);
        return this;
    }
    
    public @NotNull ValidationModelBuilder childResource(@NotNull ChildResource childResource) {
        children.add(childResource);
        return this;
    }
    
    public @NotNull ValidationModelBuilder childResources(@NotNull List<ChildResource> childResources) {
        children.addAll(childResources);
        return this;
    }
    
    public @NotNull ValidationModelBuilder setApplicablePath(@NotNull String applicablePath) {
        applicablePaths.clear();
        applicablePaths.add(applicablePath);
        return this;
    }
    
    public @NotNull ValidationModelBuilder addApplicablePath(@NotNull String applicablePath) {
        applicablePaths.add(applicablePath);
        return this;
    }
    
    public @NotNull ValidationModelBuilder addApplicablePaths(@NotNull String[] applicablePaths) {
        for (String applicablePath : applicablePaths) {
            this.applicablePaths.add(applicablePath);
        }
        return this;
    }
    
    public @NotNull ValidationModel build(@NotNull String validatedResourceType, @NotNull String source) {
        return new ValidationModelImpl(resourceProperties, validatedResourceType, applicablePaths, children, source);
    }
}
