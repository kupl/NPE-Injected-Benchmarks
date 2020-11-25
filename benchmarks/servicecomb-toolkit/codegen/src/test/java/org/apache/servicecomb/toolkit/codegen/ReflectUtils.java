/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.toolkit.codegen;

import java.lang.reflect.Field;

class ReflectUtils {

  static Object getProperty(Object obj, String propName) throws IllegalAccessException, NoSuchFieldException {

    Field propFiled = getFiled(obj.getClass(), propName);

    if (propFiled == null) {
      return null;
    }

    propFiled.setAccessible(true);
    return propFiled.get(obj);
  }

  private static Field getFiled(Class cls, String propName) throws NoSuchFieldException {
    try {
      return cls.getDeclaredField(propName);
    } catch (NoSuchFieldException e) {
      if (cls.getSuperclass() != null) {
        return getFiled(cls.getSuperclass(), propName);
      } else {
        throw new NoSuchFieldException("No such field: " + propName);
      }
    }
  }
}
