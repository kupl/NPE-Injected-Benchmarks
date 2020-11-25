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
package org.apache.sling.commons.html.it;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.sling.commons.html.HtmlParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TagsoupHtmlParserIT extends HtmlTestSupport {

    @Inject
    @Filter(value = "(&(dom=tagsoup)(sax=tagsoup))")
    private HtmlParser htmlParser;

    @Configuration
    public Option[] configuration() {
        return new Option[]{
            this.baseConfiguration(),
            newConfiguration("org.apache.sling.commons.html.internal.TagsoupHtmlParser")
                .put("parser.features", "foo=true")
                .asOption(),
        };
    }

    @Test
    public void testHtmlParser() {
        assertNotNull(htmlParser);
    }

    @Test
    public void testFeaturesConfiguration() throws IllegalAccessException {
        @SuppressWarnings("unchecked") final Map<String, Boolean> features = (Map<String, Boolean>) FieldUtils.readDeclaredField(htmlParser, "features", true);
        assertNotNull(features);
        final Boolean foo = features.get("foo");
        assertTrue(foo);
    }

}
