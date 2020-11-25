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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.sling.commons.html.AttrValue;
import org.apache.sling.commons.html.HtmlElement;
import org.apache.sling.commons.html.HtmlElementType;

public class StartTag implements HtmlElement {

    private String value;
    private Map<String, AttrValue> attributes = Collections.emptyMap();
    private boolean selfClosing;
    private static List<String> voidTags = Arrays.asList("area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr");

    public StartTag(String value, Map<String, AttrValue> attrList) {
        this(value, attrList, voidTags.contains(value));
    }

    public StartTag(String value, Map<String, AttrValue> attrList, boolean selfClosing) {
        this.value = value;
        this.selfClosing = selfClosing;
        attributes = attrList;
    }

    @Override
    public HtmlElementType getType() {
        return HtmlElementType.START_TAG;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean supportsAttributes() {
        return true;
    }

    public boolean getVoidTag() {
        return selfClosing;
    }

    @Override
    public Map<String, AttrValue> getAttributes() {
        return attributes;
    }
}