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
package org.apache.logging.log4j.catalog.api.util;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Sets the profile for the application if it hasn't already been set.
 */
public final class ProfileUtil {

    private static final String SPRING_PROFILE = "spring.profiles.active";

    private ProfileUtil() {
    }

    /**
     * Set the active profile if none has been specified.
     * @param servletContext
     */
    public static void setActiveProfile(ServletContext servletContext) {
        String springProfile = System.getProperty(SPRING_PROFILE);
        if (springProfile == null) {
            springProfile = System.getenv(SPRING_PROFILE);
        }
        if (springProfile == null) {
            springProfile = servletContext.getInitParameter(SPRING_PROFILE);
        }
        if (springProfile == null) {
            Properties props = loadProperties(servletContext);
            String activeProfile = props.getProperty(SPRING_PROFILE);
            if (activeProfile == null) {
                servletContext.setInitParameter(SPRING_PROFILE, "eclipseLink");
            }
        }
    }

private static java.util.Properties loadProperties(javax.servlet.ServletContext servletContext) {
    java.util.Properties props = new java.util.Properties();
    java.lang.String env = java.lang.System.getProperty("env");
    if (env == null) {
        env = java.lang.System.getenv("env");
    }
    java.lang.StringBuilder sb = new java.lang.StringBuilder("catalog-");
    if (env != null) {
        sb.append(env);
    }
    sb.append("config.properties");
    java.io.InputStream is = org.apache.logging.log4j.catalog.api.util.ProfileUtil.class.getClassLoader().getResourceAsStream(sb.toString());
    {
        try {
            props.load(/* NPEX_NULL_EXP */
            is);
        } catch (java.io.IOException ioe) {
            servletContext.log((("Unable to load " + sb.toString()) + ": ") + ioe.getMessage());
        }
    }
    return props;
}
}
