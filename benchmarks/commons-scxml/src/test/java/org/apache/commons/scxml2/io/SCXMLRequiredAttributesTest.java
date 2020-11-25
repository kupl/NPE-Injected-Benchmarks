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
package org.apache.commons.scxml2.io;

import java.io.StringReader;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test enforcement of required SCXML element attributes, spec https://www.w3.org/TR/2015/REC-scxml-20150901
 * <p>
 * TODO required attributes for elements:
 * <ul>
 *   <li>&lt;raise&gt; required attribute: 'id'</li>
 * </ul>
 * </p>
 */
public class SCXMLRequiredAttributesTest {

    private static final String VALID_SCXML =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" datamodel=\"jexl\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <transition target=\"fine\">\n" +
                    "      <if cond=\"true\"><log expr=\"'hello'\"/></if>\n" +
                    "    </transition>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_MISSING_VERSION =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\">\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_INVALID_VERSION =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"2.0\">\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_MISSING_IF_COND =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <transition target=\"fine\">\n" +
                    "      <if><log expr=\"'hello'\"/></if>\n" +
                    "    </transition>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_MISSING_ELSEIF_COND =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <transition target=\"fine\">\n" +
                    "      <if cond=\"false\"><elseif/><log expr=\"'hello'\"/></if>\n" +
                    "    </transition>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_MISSING_DATA_ID =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
                    "  <datamodel><data></data></datamodel>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_MISSING_ASSIGN_LOCATION =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <transition target=\"fine\">\n" +
                    "      <assign expr=\"1\"/>\n" +
                    "    </transition>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_MISSING_PARAM_NAME =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <invoke type=\"scxml\" src=\"foo\">\n" + // Note: invalid src, but not executed during test
                    "      <param expr=\"1\"/>\n" +
                    "    </invoke>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_PARAM_AND_NAME =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <invoke type=\"scxml\" src=\"foo\">\n" + // Note: invalid src, but not executed during test
                    "      <param name=\"bar\" expr=\"1\"/>\n" +
                    "    </invoke>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_MISSING_FOREACH_ARRAY =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <transition target=\"fine\">\n" +
                    "      <foreach item=\"y\"></foreach>\n" +
                    "    </transition>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_MISSING_FOREACH_ITEM =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <transition target=\"fine\">\n" +
                    "      <foreach array=\"[1,2]\"></foreach>\n" +
                    "    </transition>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    private static final String SCXML_WITH_FOREACH =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" datamodel=\"jexl\" version=\"1.0\">\n" +
                    "  <state id=\"s1\">\n" +
                    "    <transition target=\"fine\">\n" +
                    "      <foreach array=\"[1,2]\" item=\"x\"></foreach>\n" +
                    "    </transition>\n" +
                    "  </state>\n" +
                    "  <final id=\"fine\"/>\n" +
                    "</scxml>";

    @Test
    public void testValidSCXML() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(new StringReader(VALID_SCXML), null);
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        assertTrue(exec.getStatus().isFinal());
    }

    @Test
    public void testSCXMLMissingVersion() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_MISSING_VERSION), null),
                "SCXML reading should have failed due to missing version in SCXML");
        assertTrue(e.getMessage().startsWith("<scxml> is missing required attribute \"version\" value"));
    }

    @Test
    public void testSCXMLInvalidVersion() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_INVALID_VERSION), null),
                "SCXML reading should have failed due to missing version in SCXML");
        assertEquals("The <scxml> element defines an unsupported version \"2.0\", only version \"1.0\" is supported.", e.getMessage());
    }

    @Test
    public void testSCXMLMissingIfCond() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_MISSING_IF_COND), null),
                "SCXML reading should have failed due to missing if condition in SCXML");
        assertTrue(e.getMessage().startsWith("<if> is missing required attribute \"cond\" value"));
    }

    @Test
    public void testSCXMLMissingElseIfCond() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_MISSING_ELSEIF_COND), null),
                "SCXML reading should have failed due to missing elseif condition in SCXML");
        assertTrue(e.getMessage().startsWith("<elseif> is missing required attribute \"cond\" value"));
    }

    @Test
    public void testSCXMLMissingDataId() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_MISSING_DATA_ID), null),
                "SCXML reading should have failed due to missing data id in SCXML");
        assertTrue(e.getMessage().startsWith("<data> is missing required attribute \"id\" value"));
    }

    @Test
    public void testSCXMLMissingAssignLocation() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_MISSING_ASSIGN_LOCATION), null),
                "SCXML reading should have failed due to missing assign location in SCXML");
        assertTrue(e.getMessage().startsWith("<assign> is missing required attribute \"location\" value"));
    }

    @Test
    public void testSCXMLMissingParamName() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_MISSING_PARAM_NAME), null),
                "SCXML reading should have failed due to missing param name in SCXML");
        assertTrue(e.getMessage().startsWith("<param> is missing required attribute \"name\" value"));
    }

    @Test
    public void testSCXMLParamWithName() throws Exception {
        SCXMLTestHelper.parse(new StringReader(SCXML_WITH_PARAM_AND_NAME), null);
        // Note: cannot execute this instance without providing proper <invoke> src attribute
    }

    @Test
    public void testSCXMLMissingForeachArray() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_MISSING_FOREACH_ARRAY), null),
                "SCXML reading should have failed due to missing foreach array in SCXML");
        assertTrue(e.getMessage().startsWith("<foreach> is missing required attribute \"array\" value"));
    }

    @Test
    public void testSCXMLMissingForeachItem() {
        ModelException e = assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_MISSING_FOREACH_ITEM), null),
                "SCXML reading should have failed due to missing foreach item in SCXML");
        assertTrue(e.getMessage().startsWith("<foreach> is missing required attribute \"item\" value"));
    }

    @Test
    public void testSCXMLWithForEach() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(new StringReader(SCXML_WITH_FOREACH), null);
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        assertTrue(exec.getStatus().isFinal());
    }
}
