/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.logging.log4j.catalog.jpa.converter;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.catalog.api.Attribute;
import org.apache.logging.log4j.catalog.api.Constraint;
import org.apache.logging.log4j.catalog.api.plugins.ConstraintPlugins;
import org.apache.logging.log4j.catalog.jpa.model.AttributeModel;
import org.apache.logging.log4j.catalog.jpa.model.ConstraintModel;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AttributeModelConverter extends AbstractConverter<AttributeModel, Attribute> {

public org.apache.logging.log4j.catalog.api.Attribute convert(org.apache.logging.log4j.catalog.jpa.model.AttributeModel model) {
    org.apache.logging.log4j.catalog.api.Attribute attribute = new org.apache.logging.log4j.catalog.api.Attribute();
    attribute.setName(model.getName());
    attribute.setDisplayName(model.getDisplayName());
    attribute.setDescription(model.getDescription());
    attribute.setAliases(model.getAliases());
    attribute.setId(model.getId());
    attribute.setCatalogId(model.getCatalogId());
    attribute.setIndexed(model.isIndexed());
    attribute.setSortable(model.isSortable());
    attribute.setRequestContext(model.isRequestContext());
    attribute.setRequired(model.isRequired());
    attribute.setDataType(model.getDataType());
    java.util.Set<org.apache.logging.log4j.catalog.jpa.model.ConstraintModel> constraintModels = model.getConstraints();
    java.util.Set<org.apache.logging.log4j.catalog.api.Constraint> constraints = new java.util.HashSet<>();
    {
        for (org.apache.logging.log4j.catalog.jpa.model.ConstraintModel constraintModel : /* NPEX_NULL_EXP */
        constraintModels) {
            org.apache.logging.log4j.catalog.api.Constraint constraint = new org.apache.logging.log4j.catalog.api.Constraint();
            constraint.setId(constraintModel.getId());
            constraint.setConstraintType(org.apache.logging.log4j.catalog.api.plugins.ConstraintPlugins.getInstance().findByName(constraintModel.getConstraintType()));
            constraint.setValue(constraintModel.getValue());
            constraints.add(constraint);
        }
    }
    attribute.setConstraints(constraints);
    return attribute;
}
}
