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
package org.apache.sling.commons.html.impl.tag;

import java.util.Collections;
import java.util.Map;

import org.apache.sling.commons.html.AttrValue;
import org.apache.sling.commons.html.HtmlElement;
import org.apache.sling.commons.html.HtmlElementType;

public class EndOfFile implements HtmlElement {

    @Override
    public HtmlElementType getType() {
        return HtmlElementType.EOF;
    }

    @Override
    public boolean supportsAttributes() {
        return false;
    }

    @Override
    public Map<String, AttrValue> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public String getValue() {
        return "EOF";
    }

}
