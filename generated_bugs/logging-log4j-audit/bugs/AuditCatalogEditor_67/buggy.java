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
package org.apache.logging.log4j.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.catalog.api.util.ProfileUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 *
 */
@SpringBootApplication
public class AuditCatalogEditor extends SpringBootServletInitializer {
    private static final String SPRING_PROFILE = "spring.profiles.active";

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder().profiles(getActiveProfile())
            .sources(AuditCatalogEditor.class);
        System.setProperty("isEmbedded", "true");
        builder.run(args);
    }

    /**
     * Get the active profile if none has been specified.
     */
    public static String getActiveProfile() {
        String springProfile = System.getProperty(SPRING_PROFILE);
        if (springProfile == null) {
            springProfile = System.getenv(SPRING_PROFILE);
        }
        if (springProfile == null) {
            Properties props = loadProperties();
            springProfile = props.getProperty(SPRING_PROFILE);
            if (springProfile == null) {
                springProfile = "eclipseLink";
            }
        }
        return springProfile;
    }

private static java.util.Properties loadProperties() {
    java.util.Properties props = new java.util.Properties();
    java.lang.String env = java.lang.System.getProperty("env");
    if (env == null) {
        env = java.lang.System.getenv("env");
    }
    java.lang.StringBuilder sb = new java.lang.StringBuilder("catalog-");
    {
        sb.append(/* NPEX_NULL_EXP */
        env);
    }
    sb.append("config.properties");
    java.io.InputStream is = org.apache.logging.log4j.catalog.api.util.ProfileUtil.class.getClassLoader().getResourceAsStream(sb.toString());
    if (is != null) {
        try {
            props.load(is);
        } catch (java.io.IOException ioe) {
            // Ignore the error.
        }
    }
    return props;
}

}
