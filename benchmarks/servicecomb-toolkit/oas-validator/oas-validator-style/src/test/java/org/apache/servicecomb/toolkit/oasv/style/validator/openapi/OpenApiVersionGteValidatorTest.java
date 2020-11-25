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

package org.apache.servicecomb.toolkit.oasv.style.validator.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.servicecomb.toolkit.oasv.style.factory.ValidatorFactoryComponents;
import org.apache.servicecomb.toolkit.oasv.style.validator.OasStyleCheckTestBase;
import org.apache.servicecomb.toolkit.oasv.validation.api.OasSpecValidator;
import org.apache.servicecomb.toolkit.oasv.validation.api.OasViolation;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = ValidatorFactoryComponents.class)
public class OpenApiVersionGteValidatorTest extends OasStyleCheckTestBase {

  @Test
  public void testValidate() {
    OpenAPI openAPI = loadRelative("petstore-openapi-wrong.yaml");
    OasSpecValidator oasSpecValidator =
        oasSpecValidatorFactory.create(
            singleOption(OpenApiVersionGteValidator.CONFIG_KEY, "3.0.2")
        );
    List<OasViolation> violations = oasSpecValidator.validate(createContext(openAPI), openAPI);
    assertThat(violations).hasSize(1);
    assertThat(violations)
        .containsExactly(createViolation(OpenApiVersionGteValidator.ERROR + "3.0.2", "openapi", null));
  }

}

