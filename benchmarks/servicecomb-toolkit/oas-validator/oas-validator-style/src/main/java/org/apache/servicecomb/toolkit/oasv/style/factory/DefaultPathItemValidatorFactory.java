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

package org.apache.servicecomb.toolkit.oasv.style.factory;

import org.apache.servicecomb.toolkit.oasv.FactoryOptions;
import org.apache.servicecomb.toolkit.oasv.validation.api.PathItemValidator;
import org.apache.servicecomb.toolkit.oasv.validation.factory.OperationValidatorFactory;
import org.apache.servicecomb.toolkit.oasv.validation.factory.ParameterValidatorFactory;
import org.apache.servicecomb.toolkit.oasv.validation.factory.PathItemValidatorFactory;
import org.apache.servicecomb.toolkit.oasv.validation.skeleton.pathitem.PathItemOperationsValidator;
import org.apache.servicecomb.toolkit.oasv.validation.skeleton.pathitem.PathItemParametersValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DefaultPathItemValidatorFactory implements PathItemValidatorFactory {

  private final OperationValidatorFactory operationValidatorFactory;

  private final ParameterValidatorFactory parameterValidatorFactory;

  public DefaultPathItemValidatorFactory(
      OperationValidatorFactory operationValidatorFactory,
      ParameterValidatorFactory parameterValidatorFactory) {
    this.operationValidatorFactory = operationValidatorFactory;
    this.parameterValidatorFactory = parameterValidatorFactory;
  }

  @Override
  public List<PathItemValidator> create(FactoryOptions options) {
    ArrayList<PathItemValidator> validators = new ArrayList<>();

    // skeletons
    validators.add(new PathItemOperationsValidator(operationValidatorFactory.create(options)));
    validators.add(new PathItemParametersValidator(parameterValidatorFactory.create(options)));

    return Collections.unmodifiableList(validators);
  }

}
