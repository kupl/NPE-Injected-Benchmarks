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
package org.apache.commons.scxml2.model;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.EventBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class StatelessModelTest {

    /**
     * Test the stateless model, simultaneous executions, JEXL expressions
     */    
    @Test
    public void testStatelessModelSimultaneousJexl() throws Exception {
    	// parse once, use many times
        SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/env/jexl/stateless-01.xml");
        SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor(scxml);
        exec01.go();
        SCXMLExecutor exec02 = SCXMLTestHelper.getExecutor(scxml);
        exec02.go();
        Assertions.assertFalse(exec01 == exec02);
        runSimultaneousTest(exec01, exec02);
    }

    /**
     * Test the stateless model, sequential executions, JEXL expressions
     */    
    @Test
    public void testStatelessModelSequentialJexl() throws Exception {
        // rinse and repeat
        SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/env/jexl/stateless-01.xml");
        for (int i = 0; i < 3; i++) {
            SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor(scxml);
            exec01.go();
            runSequentialTest(exec01);
        }
    }

    /**
     * Test sharing a single SCXML object between two executors
     */    
    @Test
    public void testStatelessModelParallelSharedSCXML() throws Exception {
        SCXML scxml01par = SCXMLTestHelper.parse("org/apache/commons/scxml2/model/stateless-parallel-01.xml");
        SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor(scxml01par);
        exec01.go();
        SCXMLExecutor exec02 = SCXMLTestHelper.getExecutor(scxml01par);
        exec02.go();
        Assertions.assertFalse(exec01 == exec02);

        Set<EnterableState> currentStates = exec01.getStatus().getStates();
        checkParallelStates(currentStates, "state1.init", "state2.init", "exec01");

        currentStates = exec02.getStatus().getStates();
        checkParallelStates(currentStates, "state1.init", "state2.init", "exec02");

        currentStates = fireEvent("state1.event", exec01);
        checkParallelStates(currentStates, "state1.final", "state2.init", "exec01");

        currentStates = fireEvent("state2.event", exec02);
        checkParallelStates(currentStates, "state1.init", "state2.final", "exec02");

        currentStates = fireEvent("state2.event", exec01);
        checkParallelStates(currentStates, "next", null, "exec01");

        currentStates = fireEvent("state1.event", exec02);
        checkParallelStates(currentStates, "next", null, "exec02");
    }

    /**
     * TODO: Test sharing two SCXML objects between one executor (not recommended)
     *
    @Test
    public void testStatelessModelParallelSwapSCXML() throws Exception {
        SCXML scxml01par = SCXMLTestHelper.parse("org/apache/commons/scxml2/model/stateless-parallel-01.xml");
        SCXML scxml02par = SCXMLTestHelper.parse("org/apache/commons/scxml2/model/stateless-parallel-01.xml");
        SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor(scxml01par);
        exec01.go();
        Assertions.assertTrue(scxml01par != scxml02par);

        Set<EnterableState> currentStates = exec01.getStatus().getStates();
        checkParallelStates(currentStates, "state1.init", "state2.init", "exec01");

        currentStates = fireEvent("state1.event", exec01);
        checkParallelStates(currentStates, "state1.final", "state2.init", "exec01");
        exec01.setStateMachine(scxml02par);
        exec01.addListener(scxml02par, new SimpleSCXMLListener());
        currentStates = fireEvent("state2.event", exec01);
        checkParallelStates(currentStates, "next", null, "exec01");
    }
     */

    private void checkParallelStates(Set<EnterableState> currentStates,
            String s1, String s2, String label) {
        Iterator<EnterableState> i = currentStates.iterator();
        Assertions.assertTrue(i.hasNext(), "Not enough states");
        String cs1 = i.next().getId();
        String cs2;
        if (s2 != null) {
            Assertions.assertTrue(i.hasNext(), "Not enough states, found one state: " + cs1);
            cs2 = i.next().getId();
            Assertions.assertFalse(i.hasNext(), "Too many states");
            if (s2.equals(cs2)) {
                cs2 = null;
            } else if (s1.equals(cs2)) {
                cs2 = null;
            } else {
                Assertions.fail(label + " in unexpected state " + cs2);
            }
        } else {
            Assertions.assertFalse(i.hasNext(), "Too many states");
        }
        if (s1 != null && s1.equals(cs1)) {
            return;
        }
        if (s2 != null && s2.equals(cs1)) {
            return;
        }
        Assertions.fail(label + " in unexpected state " + cs1);
    }

    private void runSimultaneousTest(SCXMLExecutor exec01, SCXMLExecutor exec02) throws Exception {
        //// Interleaved
        // exec01
        Set<EnterableState> currentStates = exec01.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = fireEvent("done.state.ten", exec01);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty", currentStates.iterator().next().getId());
        // exec02
        currentStates = exec02.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", currentStates.iterator().next().getId());
        // exec01
        currentStates = fireEvent("done.state.twenty", exec01);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("thirty", currentStates.iterator().next().getId());
        // exec02
        currentStates = fireEvent("done.state.ten", exec02);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty", currentStates.iterator().next().getId());
        currentStates = fireEvent("done.state.twenty", exec02);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("thirty", currentStates.iterator().next().getId());
    }

    private void runSequentialTest(SCXMLExecutor exec) throws Exception {
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", (currentStates.iterator().
            next()).getId());
        currentStates = fireEvent("done.state.ten", exec);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty", (currentStates.iterator().
            next()).getId());
        currentStates = fireEvent("done.state.twenty", exec);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("thirty", (currentStates.iterator().
            next()).getId());
    }

    private Set<EnterableState> fireEvent(String name, SCXMLExecutor exec) throws Exception {
        TriggerEvent[] evts = {new EventBuilder(name, TriggerEvent.SIGNAL_EVENT).build()};
        exec.triggerEvents(evts);
        return exec.getStatus().getStates();
    }
}

