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
package org.apache.sling.cms.transformer.internal;

import java.io.IOException;
import java.io.InputStream;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.cms.transformer.TransformationHandler;
import org.osgi.service.component.annotations.Component;

import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Positions;

/**
 * A transformation handler to crop images
 */
@Component(service = TransformationHandler.class, property = { TransformationHandler.HANDLER_RESOURCE_TYPE
        + "=sling-cms/components/caconfig/transformationhandlers/crop" }, immediate = true)
public class CropHandler implements TransformationHandler {

    public static final String PN_POSITION = "position";

    @Override
    public String getResourceType() {
        return "sling-cms/components/caconfig/transformationhandlers/crop";
    }

    @Override
    public void handle(Builder<? extends InputStream> builder, Resource config) throws IOException {
        String positionStr = config.getValueMap().get(PN_POSITION, "CENTER").toUpperCase();
        try {
            Positions pos = Positions.valueOf(positionStr);
            builder.crop(pos);
        } catch (IllegalArgumentException e) {
            throw new IOException("Unable to load crop position from " + positionStr, e);
        }
    }

}
