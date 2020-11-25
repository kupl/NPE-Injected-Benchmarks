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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides a Stream of HtmlElement tokens
 * 
 */
public class Html {

    private Html() {
    }
    
    public static Stream<HtmlElement> stream(String input) {
        return stream(new ByteArrayInputStream(input.getBytes()));
    }

    public static Stream<HtmlElement> stream(InputStream is) {
        return  stream(new HtmlIterator(is));
    }

    public static Stream<HtmlElement> stream(InputStream is, String encoding) {
        return stream(new HtmlIterator(is, encoding));
    }

    private static Stream<HtmlElement> stream(HtmlIterator iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

}
