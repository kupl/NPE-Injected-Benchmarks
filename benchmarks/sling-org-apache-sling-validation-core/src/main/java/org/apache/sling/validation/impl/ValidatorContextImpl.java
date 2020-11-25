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
package org.apache.sling.validation.impl;

import java.util.ResourceBundle;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.validation.spi.ValidatorContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValidatorContextImpl implements ValidatorContext {

    private final @NotNull String location;
    private final int severity;
    private final @NotNull ValueMap valueMap;
    private final Resource resource;
    private final @NotNull ResourceBundle defaultResourceBundle;

    public ValidatorContextImpl(@NotNull String location, int severity, @NotNull ValueMap valueMap, Resource resource, @NotNull ResourceBundle defaultResourceBundle) {
        super();
        this.location = location;
        this.severity = severity;
        this.valueMap = valueMap;
        this.resource = resource;
        this.defaultResourceBundle = defaultResourceBundle;
    }

    @Override
    @NotNull
    public String getLocation() {
        return location;
    }

    @Override
    @NotNull
    public ValueMap getValueMap() {
        return valueMap;
    }

    @Override
    @Nullable
    public Resource getResource() {
        return resource;
    }

    @Override
    public int getSeverity() {
        return severity;
    }

    @Override
    public @NotNull ResourceBundle getDefaultResourceBundle() {
        return defaultResourceBundle;
    }

}
