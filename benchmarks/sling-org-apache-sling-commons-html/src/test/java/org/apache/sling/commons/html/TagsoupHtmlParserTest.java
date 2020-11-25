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
package org.apache.sling.commons.html;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.sling.commons.html.internal.TagsoupHtmlParser;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TagsoupHtmlParserTest {
    
    TagsoupHtmlParser htmlParser;
    InputStream stream;
    
    /*
     * Japanese (google) translation of 'Don't forget me this weekend!' 
     * standard text of xml sample note.xml
     */
    private static final String MESSAGE ="この週末私を忘れないで!";
    
    @Before
    public void setup() {
        stream = getClass().getResourceAsStream("/note.xml");
        htmlParser = new TagsoupHtmlParser();
    }

    @Test
    public void testEncodingSupport() throws SAXException {
        htmlParser.parse(stream, "UTF-8", new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                String message = new String(ch, start, length).trim();
                if (!message.isEmpty()) {
                    assertTrue(message.equals(MESSAGE));
                }
            }
        });
    }
    
    @Test
    public void testEncodingSupportFailure() throws SAXException {
        htmlParser.parse(stream, "ISO8859-1", new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                String message = new String(ch, start, length).trim();
                if (!message.isEmpty()) {
                    assertTrue(!message.equals(MESSAGE));
                }
            }
        });
    }

}
