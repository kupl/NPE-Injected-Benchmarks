/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.env.groovy;

import java.net.URL;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.EnterableState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 * Testing Groovy initial base script usage and validating serializable
 */
public class SerializableInitialBaseScriptTest {

    /**
     * Testing Groovy initial base script usage and validating serializable
     */
    @Test
    public void testSerializableInitialBaseScriptSample() throws Exception {
        URL scxml = SCXMLTestHelper.getResource("org/apache/commons/scxml2/env/groovy/serializable-initial-base-script.xml");
    	SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml, new GroovyEvaluator(true));
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state1", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "foo.bar.baz");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state4", currentStates.iterator().next().getId());
    }
}
