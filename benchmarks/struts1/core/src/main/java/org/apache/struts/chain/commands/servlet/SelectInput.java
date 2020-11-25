/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts.chain.commands.servlet;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.chain.commands.AbstractSelectInput;
import org.apache.struts.chain.contexts.ActionContext;
import org.apache.struts.chain.contexts.ServletActionContext;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;

/**
 * <p>Validate the properties of the form bean for this request.  If there are
 * any validation errors, execute the child commands in our chain; otherwise,
 * proceed normally.</p>
 *
 * @version $Rev$ $Date: 2005-05-07 12:11:38 -0400 (Sat, 07 May 2005)
 *          $
 */
public class SelectInput extends AbstractSelectInput {
    // ------------------------------------------------------- Protected Methods

    /**
     * <p>Create and return a <code>ForwardConfig</code> representing the
     * specified module-relative destination.</p>
     *
     * @param context      The context for this request
     * @param moduleConfig The <code>ModuleConfig</code> for this request
     * @param uri          The module-relative URI to be the destination
     */
    protected ForwardConfig forward(ActionContext context,
        ModuleConfig moduleConfig, String uri) {
        return (new ActionForward(null, uri, false, moduleConfig.getPrefix()));
    }

    protected String getErrorMessage(ActionContext context,
        ActionConfig actionConfig) {
        ServletActionContext servletActionContext =
            (ServletActionContext) context;

        // Retrieve internal message resources
        ActionServlet servlet = servletActionContext.getActionServlet();
        MessageResources resources = servlet.getInternal();

        return resources.getMessage("inputUnknown", actionConfig.getPath(), actionConfig.getInput());
    }

}
