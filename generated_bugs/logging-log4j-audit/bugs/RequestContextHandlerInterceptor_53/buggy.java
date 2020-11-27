/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.audit.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.audit.request.ChainedMapping;
import org.apache.logging.log4j.audit.request.RequestContextMapping;
import org.apache.logging.log4j.audit.request.RequestContextMappings;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * May be used instead of the RequestContextFilter to convert RequestContext headers to ThreadContext variables.
 */
public class RequestContextHandlerInterceptor implements HandlerInterceptor {

    private static final Logger logger = LogManager.getLogger(RequestContextHandlerInterceptor.class);
    private final RequestContextMappings mappings;
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    public RequestContextHandlerInterceptor(Class<?> clazz) {
        mappings = new RequestContextMappings(clazz);
    }

    @Override
public boolean preHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, java.lang.Object o) {
    org.apache.logging.log4j.audit.rest.RequestContextHandlerInterceptor.logger.trace("Starting request {}", request.getRequestURI());
    java.util.Enumeration<java.lang.String> headers = request.getHeaderNames();
    while (headers.hasMoreElements()) {
        java.lang.String name = headers.nextElement();
        org.apache.logging.log4j.audit.request.RequestContextMapping mapping = mappings.getMappingByHeader(name);
        org.apache.logging.log4j.audit.rest.RequestContextHandlerInterceptor.logger.debug("Got Mapping:{} for Header:{}", mapping, name);
        {
            if (/* NPEX_NULL_EXP */
            mapping.isChained()) {
                org.apache.logging.log4j.ThreadContext.put(mapping.getChainKey(), request.getHeader(name));
                org.apache.logging.log4j.audit.rest.RequestContextHandlerInterceptor.logger.debug("Setting Context Key:{} with value:{}", mapping.getChainKey(), request.getHeader(name));
                java.lang.String value = ((org.apache.logging.log4j.audit.request.ChainedMapping) (mapping)).getSupplier().get();
                org.apache.logging.log4j.ThreadContext.put(mapping.getFieldName(), value);
                org.apache.logging.log4j.audit.rest.RequestContextHandlerInterceptor.logger.debug("Setting Context Key:{} with value:{}", mapping.getFieldName(), value);
            } else {
                org.apache.logging.log4j.ThreadContext.put(mapping.getFieldName(), request.getHeader(name));
                org.apache.logging.log4j.audit.rest.RequestContextHandlerInterceptor.logger.debug("Setting Context Key:{} with value:{}", mapping.getFieldName(), request.getHeader(name));
            }
        }
    } 
    if (org.apache.logging.log4j.audit.rest.RequestContextHandlerInterceptor.logger.isTraceEnabled()) {
        startTime.set(java.lang.System.nanoTime());
    }
    return true;
}

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) {
        if (logger.isTraceEnabled()) {
            long elapsed = System.nanoTime() - startTime.get();
            StringBuilder sb = new StringBuilder("Request ").append(request.getRequestURI()).append(" completed in ");
            ElapsedUtil.addElapsed(elapsed, sb);
            logger.trace(sb.toString());
            startTime.remove();
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        ThreadContext.clearMap();
    }
}
