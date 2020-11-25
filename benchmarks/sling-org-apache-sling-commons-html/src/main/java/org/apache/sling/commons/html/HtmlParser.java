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

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The HTML parser is a service to parse HTML and generate
 * SAX events or a Document out of the HTML.
 */
public interface HtmlParser {

    /**
     * Parse HTML and send SAX events.
     *
     * @param inputStream    The input stream
     * @param encoding       Encoding of the input stream, <code>null</code> for default encoding.
     * @param contentHandler Content handler receiving the SAX events. The content handler might also
     *                       implement the lexical handler interface.
     * @throws SAXException Exception thrown when parsing fails.
     */
    void parse(InputStream inputStream, String encoding, ContentHandler contentHandler) throws SAXException;

    /**
     * Parse HTML and return a DOM Document.
     *
     * @param systemId    The system id
     * @param inputStream The input stream
     * @param encoding    Encoding of the input stream, <code>null</code> for default encoding.
     * @return A DOM Document built from parsed HTML or <code>null</code>
     * @throws IOException Exception thrown when parsing fails.
     */
    Document parse(String systemId, InputStream inputStream, String encoding) throws IOException;

}
