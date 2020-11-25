/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
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
package org.apache.creadur.whisker.model;

import junit.framework.TestCase;

public class TestDescriptorPrimaryOnly extends TestCase {

    DescriptorBuilderForTesting builder;
    Descriptor subject;

    protected void setUp() throws Exception {
        super.setUp();
        builder = new DescriptorBuilderForTesting();
        builder.withThirdParty().withDirectory(".");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsPrimaryOnlyWithThirdPartyResources() throws Exception {
        subject = builder.build();
        builder.withThirdPartyDirectory("lib");
        assertFalse("Work is not primary only when third party resources exist.", subject.isPrimaryOnly());
    }

    public void testIsPrimaryOnlyWithoutThirdPartyResources() throws Exception {
        subject = builder.build();
        assertTrue("Work is primary only when no third party resources exist.",
                subject.isPrimaryOnly());
    }

    public void testIsPrimaryOnlyWithoutResources() throws Exception {
        subject = builder.build();

        builder.contents.clear();

        assertTrue("Work is primary only when no third party resources exist.",
                subject.isPrimaryOnly());
    }

}