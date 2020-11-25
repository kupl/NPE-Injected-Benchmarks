/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.tag.common.core;

import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * <p>Tag handler for &lt;declaregt; in JSTL.  This tag handler is
 * essentially a runtime no-op as far as tag logic is concerned; the
 * only important functionality of the tag is to expose a scripting
 * variable for an existing scoped attribute.</p>
 *
 * @author Shawn Bayern
 */

public class DeclareTag extends TagSupport {

    /*
     * We're not identical to TagSupport only because we need to
     * accept an additional "type" attribute.
     */

    public void setType(String x) {
    }
}
