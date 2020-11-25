/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.commons.html.util;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.sling.commons.html.AttrValue;
import org.apache.sling.commons.html.HtmlElement;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;

/**
 * Utility Class for the TagstreamHTMLParser to generate SAX events
 * 
 *
 */
public class HtmlSAXSupport implements Consumer<HtmlElement> {

    private static final DefaultHandler2 handler = new DefaultHandler2();

    private ContentHandler contentHandler = handler;
    private LexicalHandler lexicalHandler = handler;

    public HtmlSAXSupport(ContentHandler ch, final LexicalHandler lh) {
        if (ch != null) {
            contentHandler = ch;
        }
        if (lh != null) {
            lexicalHandler = lh;
        }
    }

    @Override
    public void accept(HtmlElement element) {
        try {
            String value = element.getValue();
            switch (element.getType()) {
            case COMMENT:
                lexicalHandler.comment(value.toCharArray(), 0, value.length());
                break;
            case DOCTYPE:
                break;
            case END_TAG:
                lexicalHandler.endEntity(value);
                contentHandler.endElement("", value, value);
                break;
            case EOF:
                contentHandler.endDocument();
                break;
            case START_TAG:
                if (value.startsWith("?")) {
                    if (!value.equalsIgnoreCase("?xml")) {
                        contentHandler.processingInstruction(value, attrsToString(element.getAttributes()));
                    }
                    break;
                }
                lexicalHandler.startEntity(value);
                contentHandler.startElement("", value, value, HtmlSAXSupport.convert(element.getAttributes()));
                break;
            case TEXT:
                contentHandler.characters(value.toCharArray(), 0, value.toCharArray().length);
                break;
            default:
                break;
            }
        } catch (SAXException se) {
            //se.printStackTrace();
        }

    }

    public static Attributes convert(Map<String, AttrValue> attributes) {
        Attributes2Impl response = new Attributes2Impl();
        attributes.entrySet().forEach(attr -> response.addAttribute("", attr.getKey(), attr.getKey(), "xsi:String",
                attr.getValue().toString()));
        return response;
    }

    public void startDocument() throws IOException {
        try {
            contentHandler.startDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    public void endDocument() throws IOException {
        try {
            contentHandler.endDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }
    
    private String attrsToString(Map<String, AttrValue> attributes) {
        StringBuilder sb = new StringBuilder();
        attributes.entrySet().forEach(attr -> sb.append(attr.toString()));
        return sb.toString();
    }

}
