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
package org.apache.sling.commons.html.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.sling.commons.html.HtmlParser;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.ccil.cowan.tagsoup.Parser;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

@Component(
    property = {
        "dom=tagsoup",
        "sax=tagsoup",
        "api=4"
    }
)
@Designate(
    ocd = TagsoupHtmlParserConfiguration.class
)
public class TagsoupHtmlParser implements HtmlParser {

    private Map<String, Boolean> features = Collections.synchronizedMap(new LinkedHashMap<>());

    @Activate
    private void activate(final TagsoupHtmlParserConfiguration configuration) {
        configure(configuration);
    }

    @Modified
    private void modified(final TagsoupHtmlParserConfiguration configuration) {
        configure(configuration);
    }

    @Deactivate
    private void deactivate() {
        features.clear();
    }

    private void configure(final TagsoupHtmlParserConfiguration configuration) {
        features.clear();
        final Map<String, String> map = PropertiesUtil.toMap(configuration.parser_features(), new String[]{});
        for (final String key : map.keySet()) {
            features.put(key, Boolean.valueOf(map.get(key)));
        }
    }

    /**
     * @see org.apache.sling.commons.html.HtmlParser#parse(java.io.InputStream, java.lang.String, org.xml.sax.ContentHandler)
     */
    @Override
    public void parse(final InputStream stream, final String encoding, final ContentHandler contentHandler) throws SAXException {
        final Parser parser = buildParser(features, contentHandler);
        final InputSource source = new InputSource(stream);
        source.setEncoding(encoding);
        try {
            parser.parse(source);
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * @see org.apache.sling.commons.html.HtmlParser#parse(java.lang.String, java.io.InputStream, java.lang.String)
     */
    @Override
    public Document parse(String systemId, InputStream stream, String encoding) throws IOException {
        final DOMBuilder builder = new DOMBuilder();

        final InputSource source = new InputSource(stream);
        source.setEncoding(encoding);
        source.setSystemId(systemId);

        try {
            final Parser parser = buildParser(features, builder);
            parser.parse(source);
        } catch (SAXException se) {
            if (se.getCause() instanceof IOException) {
                throw (IOException) se.getCause();
            }
            throw new IOException("Unable to parse HTML", se);
        }
        return builder.getDocument();
    }

    private Parser buildParser(final Map<String, Boolean> features, final ContentHandler contentHandler) throws SAXException {
        final Parser parser = new Parser();
        parser.setContentHandler(contentHandler);
        if (contentHandler instanceof LexicalHandler) {
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", contentHandler);
        }
        for (final String key : features.keySet()) {
            parser.setFeature(key, features.get(key));
        }
        return parser;
    }

}
