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
package org.apache.sling.models.impl.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Path;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ResourcePath;
import org.apache.sling.models.spi.AcceptsNullName;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.models.spi.injectorspecific.AbstractInjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.StaticInjectAnnotationProcessorFactory;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(property=Constants.SERVICE_RANKING+":Integer=2500", service={Injector.class, StaticInjectAnnotationProcessorFactory.class, AcceptsNullName.class})
public class ResourcePathInjector extends AbstractInjector implements Injector, AcceptsNullName,
        StaticInjectAnnotationProcessorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcePathInjector.class);

    @Override
    public @NotNull String getName() {
        return "resource-path";
    }

    @Override
public java.lang.Object getValue(@org.jetbrains.annotations.NotNull
java.lang.Object adaptable, java.lang.String name, @org.jetbrains.annotations.NotNull
java.lang.reflect.Type declaredType, @org.jetbrains.annotations.NotNull
java.lang.reflect.AnnotatedElement element, @org.jetbrains.annotations.NotNull
org.apache.sling.models.spi.DisposalCallbackRegistry callbackRegistry) {
    java.lang.String[] resourcePaths = null;
    org.apache.sling.models.annotations.Path pathAnnotation = element.getAnnotation(org.apache.sling.models.annotations.Path.class);
    org.apache.sling.models.annotations.injectorspecific.ResourcePath resourcePathAnnotation = element.getAnnotation(org.apache.sling.models.annotations.injectorspecific.ResourcePath.class);
    if (pathAnnotation != null) {
        resourcePaths = getPathsFromAnnotation(pathAnnotation);
    } else {
        {
            resourcePaths = getPathsFromAnnotation(/* NPEX_NULL_EXP */
            resourcePathAnnotation);
        }
    }
    if (org.apache.commons.lang3.ArrayUtils.isEmpty(resourcePaths) && (name != null)) {
        // try the valuemap
        org.apache.sling.api.resource.ValueMap map = getValueMap(adaptable);
        if (map != null) {
            resourcePaths = map.get(name, java.lang.String[].class);
        }
    }
    if (org.apache.commons.lang3.ArrayUtils.isEmpty(resourcePaths)) {
        // could not find a path to inject
        return null;
    }
    org.apache.sling.api.resource.ResourceResolver resolver = getResourceResolver(adaptable);
    if (resolver == null) {
        return null;
    }
    java.util.List<org.apache.sling.api.resource.Resource> resources = getResources(resolver, resourcePaths, name);
    if ((resources == null) || resources.isEmpty()) {
        return null;
    }
    // unwrap/wrap if necessary
    if (isDeclaredTypeCollection(declaredType)) {
        return resources;
    }
    if ((declaredType instanceof java.lang.Class<?>) && ((java.lang.Class<?>) (declaredType)).isArray()) {
        return resources.toArray(new org.apache.sling.api.resource.Resource[0]);
    }
    if (resources.size() == 1) {
        return resources.get(0);
    } else {
        // multiple resources to inject, but field is not a list
        org.apache.sling.models.impl.injectors.ResourcePathInjector.LOG.warn("Cannot inject multiple resources into field {} since it is not declared as a list", name);
        return null;
    }
}

    private List<Resource> getResources(ResourceResolver resolver, String[] paths, String fieldName) {
        List<Resource> resources = new ArrayList<>();
        for (String path : paths) {
            Resource resource = resolver.getResource(path);
            if (resource != null) {
                resources.add(resource);
            } else {
                LOG.warn("Could not retrieve resource at path {} for field {}. Since it is required it won't be injected.",
                        path, fieldName);
                // all resources should've been injected. we stop
                return null;
            }
        }
        return resources;
    }

    /**
     * Obtains the paths from the annotations
     * @param annotation
     * @return
     */
    private String[] getPathsFromAnnotation(Path pathAnnotation) {
        String[] resourcePaths = null;
        if (StringUtils.isNotEmpty(pathAnnotation.value())) {
            resourcePaths = new String[] { pathAnnotation.value() };
        } else {
            resourcePaths = pathAnnotation.paths();
        }
        return resourcePaths;
    }

    /**
     * Obtains the paths from the annotations
     * @param annotation
     * @return
     */
    private String[] getPathsFromAnnotation(ResourcePath resourcePathAnnotation) {
        String[] resourcePaths = null;
        if (StringUtils.isNotEmpty(resourcePathAnnotation.path())) {
            resourcePaths = new String[] { resourcePathAnnotation.path() };
        } else {
            resourcePaths = resourcePathAnnotation.paths();
        }
        return resourcePaths;
    }

    @Override
    public InjectAnnotationProcessor2 createAnnotationProcessor(AnnotatedElement element) {
        // check if the element has the expected annotation
        ResourcePath annotation = element.getAnnotation(ResourcePath.class);
        if (annotation != null) {
            return new ResourcePathAnnotationProcessor(annotation);
        }
        return null;
    }

    private static class ResourcePathAnnotationProcessor extends AbstractInjectAnnotationProcessor2 {

        private final ResourcePath annotation;

        public ResourcePathAnnotationProcessor(ResourcePath annotation) {
            this.annotation = annotation;
        }

        @Override
        public String getName() {
            // since null is not allowed as default value in annotations, the
            // empty string means, the default should be used!
            if (annotation.name().isEmpty()) {
                return null;
            }
            return annotation.name();
        }

        @Override
        @SuppressWarnings("deprecation")
        public Boolean isOptional() {
            return annotation.optional();
        }

        @Override
        public InjectionStrategy getInjectionStrategy() {
            return annotation.injectionStrategy();
        }
    }

}
