/*-
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sling.query.impl.function;

import java.util.Iterator;
import java.util.function.Predicate;

import org.apache.sling.query.api.internal.IteratorToIteratorFunction;
import org.apache.sling.query.api.internal.Option;
import org.apache.sling.query.impl.iterator.FilteringIterator;

public class EvenFunction<T> implements IteratorToIteratorFunction<T> {

    private final boolean even;

    public EvenFunction(boolean even) {
        this.even = even;
    }

    @Override
    public Iterator<Option<T>> apply(Iterator<Option<T>> resources) {
        return new FilteringIterator<>(resources, new EvenPredicate<>(even));
    }

    private static class EvenPredicate<T> implements Predicate<T> {
        private boolean accept;

        public EvenPredicate(boolean firstState) {
            accept = firstState;
        }

        @Override
        public boolean test(T element) {
            boolean oldAccept = accept;
            accept = !accept;
            return oldAccept;
        }
    }
}