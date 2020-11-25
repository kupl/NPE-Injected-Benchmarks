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
package org.apache.servicemix.store.base;

import org.apache.servicemix.store.StoreFactory;
import org.apache.servicemix.store.StoreListener;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author: iocanel
 */
public abstract class BaseStoreFactory implements StoreFactory {

    protected Set<StoreListener> storeListeners = new LinkedHashSet<StoreListener>();

    public Set<StoreListener> getStoreListeners() {
        return storeListeners;
    }

    public void setStoreListeners(Set<StoreListener> storeListeners) {
        this.storeListeners = storeListeners;
    }
}
