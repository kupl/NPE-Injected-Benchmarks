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

package org.apache.taglibs.standard.tag.compat.sql;

import java.util.Date;

import jakarta.el.ValueExpression;
import jakarta.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.sql.DateParamTagSupport;
import org.apache.taglibs.standard.util.ExpressionUtil;

/**
 * Implementation of JSTL 1.0 {@code <dateParam>} using the container's EL implementation.
 */
public class DateParamTag extends DateParamTagSupport {
    private ValueExpression valueExpression;
    private ValueExpression typeExpression;

    @Override
    public int doStartTag() throws JspException {
        value = ExpressionUtil.evaluate(valueExpression, pageContext);
        type = ExpressionUtil.evaluate(typeExpression, pageContext);

        return super.doStartTag();
    }

    @Override
    public void release() {
        super.release();

        valueExpression = null;
        typeExpression = null;
    }

    public void setValue(String expression) {
        valueExpression = ExpressionUtil.createValueExpression(pageContext, expression, Date.class);
    }

    public void setType(String expression) {
        typeExpression = ExpressionUtil.createValueExpression(pageContext, expression, String.class);
    }
}
