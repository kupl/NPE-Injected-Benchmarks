/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.jacob.oo;


import java.lang.reflect.Method;
import java.util.Set;

import org.apache.ode.jacob.Message;
import org.apache.ode.jacob.MessageListener;


/**
 * Base-class for method-list objects. Method-lists objects should extends this
 * class <em>and</em> implement one <code>Channel</code> interface.
 */
@SuppressWarnings("serial")
public abstract class ChannelListener implements MessageListener {

    public void onMessage(Message msg) {
        Method action = ClassUtil.findActionMethod(getImplementedMethods()).evaluate(msg, Method.class);
        try {
            if (action != null && this instanceof ReceiveProcess) {
                action.invoke(((ReceiveProcess)this).getReceiver(), (Object[])msg.getBody());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public Set<Method> getImplementedMethods() {
        return null;
    }

}
