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
package org.apache.sling.commons.html;

import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.sling.commons.html.impl.parser.ParseException;
import org.apache.sling.commons.html.impl.parser.TagParser;

/**
 * Pull based iterator which processes an input stream and converts the stream
 * into a series of HtmlElement Tokens
 * 
 */
public class HtmlIterator implements Iterator<HtmlElement> {

    private HtmlElement current;

    boolean eof = false;
    private TagParser parser;

    /**
     * Convert InputStream into a series of HtmlElement tokens, using the systems
     * default character encoding
     * 
     * @param is
     */
    public HtmlIterator(InputStream is) {
        parser = new TagParser(is);
    }

    /**
     * Convert InputStream into a series of HtmlElement tokens, using the provided
     * character encoding
     * 
     * @param is
     * @param encoding
     */
    public HtmlIterator(InputStream is, String encoding) {
        parser = new TagParser(is, encoding);
    }

    /**
     * searches for and indicates whether the next HtmlElement has been found.
     */
    @Override
    public boolean hasNext() {
        if (current == null && !eof) {
            return seek();
        }
        return !eof;
    }

    @Override
    /**
     * returns the next element from the input stream, will return the token that
     * has been identified by the hasNext() method if that was used first. Will
     * return a NoSuchElement Exception if called on a stream that has no further
     * content
     * 
     */
    public HtmlElement next() {
        if (current == null && !eof) {
            seek();
        }
        if (current == null || eof) {
            throw new NoSuchElementException();
        }
        HtmlElement response = current;
        current = null;
        return response;
    }

    private boolean seek() {
        if (eof) {
            return false;
        }
        try {
            current = parser.element();
            eof = (current.getType() == HtmlElementType.EOF);
            return !eof;
        } catch (ParseException e) {
        }
        return false;
    }

}