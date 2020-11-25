/*
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
package org.apache.sling.validation.impl.it.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;

import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.sling.servlets.post.SlingPostConstants;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.osgi.OsgiConsoleClient;
import org.apache.sling.validation.testservices.internal.ValidationPostOperation;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * These tests leverage the {@link ValidationPostOperation} to validate the given request parameters.
 * The according validation model enforces the properties "field1" matching regex=^\\\p{Upper}+$ and "field2" (having an arbitrary value).
 *
 */
public class ValidationServiceIT {

    private static OsgiConsoleClient slingClient;

    @BeforeClass
    public static void setupOnce() throws IOException, ClientException, TimeoutException, InterruptedException {
        URI uri = URI.create(String.format("http://localhost:%s", Integer.getInteger("http.port")));
        slingClient = new OsgiConsoleClient(uri, "admin", "admin");
        
        // wait until the model from the validation.test-services bundle has been deployed
        slingClient.waitExists("/apps/sling/validation/models/model1", 20000, 200);
        
        // also wait for the contained OSGi services to be registered, (see https://issues.apache.org/jira/browse/SLING-7297)
        // since this is not yet supported in a release version just wait wait until all services came up as well by adding a little sleep time on top
        Thread.sleep(2000);
    }

    @Test
    public void testValidRequestModel1() throws IOException, JsonException, ClientException {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart("sling:resourceType", new StringBody("validation/test/resourceType1", ContentType.DEFAULT_TEXT));
        entityBuilder.addPart("field1", new StringBody("HELLOWORLD", ContentType.DEFAULT_TEXT));
        entityBuilder.addPart("field2", new StringBody("30.01.1988", ContentType.DEFAULT_TEXT));
        entityBuilder.addPart(SlingPostConstants.RP_OPERATION, new StringBody("validation", ContentType.DEFAULT_TEXT));
        SlingHttpResponse response = slingClient.doPost("/validation/testing/fakeFolder1/resource", entityBuilder.build(), null, 200);
        JsonObject jsonResponse = Json.createReader(new StringReader(response.getContent())).readObject();
        assertTrue(jsonResponse.getBoolean("valid"));
    }

    @Test
    public void testInvalidRequestModel1() throws IOException, JsonException, ClientException {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart("sling:resourceType", new StringBody("validation/test/resourceType1", ContentType.DEFAULT_TEXT));
        entityBuilder.addPart("field1", new StringBody("Hello World", ContentType.DEFAULT_TEXT));
        entityBuilder.addPart(SlingPostConstants.RP_OPERATION, new StringBody("validation", ContentType.DEFAULT_TEXT));
        SlingHttpResponse response = slingClient.doPost("/validation/testing/fakeFolder1/resource", entityBuilder.build(), null, 200);
        JsonObject jsonResponse = Json.createReader(new StringReader(response.getContent())).readObject();
        assertFalse(jsonResponse.getBoolean("valid"));
        JsonObject failure = jsonResponse.getJsonArray("failures").getJsonObject(0);
        assertEquals("Property does not match the pattern \"^\\p{Upper}+$\".", failure.getString("message"));
        assertEquals("field1", failure.getString("location"));
        assertEquals(10, failure.getInt("severity"));
        failure = jsonResponse.getJsonArray("failures").getJsonObject(1);
        assertEquals("Missing required property with name \"field2\".", failure.getString("message"));
        assertEquals("", failure.getString("location")); // location is empty as the property is not found (property name is part of the message rather)
        assertEquals(0, failure.getInt("severity"));
    }

    @Test
    public void testPostProcessorWithInvalidModel() throws IOException, JsonException, ClientException {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart("sling:resourceType", new StringBody("validation/test/resourceType1", ContentType.DEFAULT_TEXT));
        entityBuilder.addPart("field1", new StringBody("Hello World", ContentType.DEFAULT_TEXT));
        Header header = new BasicHeader("Accept", "application/json");
        SlingHttpResponse response = slingClient.doPost("/content/validated/invalidresource", entityBuilder.build(), Collections.singletonList(header), 500);
        JsonObject jsonResponse = Json.createReader(new StringReader(response.getContent())).readObject();
        JsonObject error = jsonResponse.getJsonObject("error");
        assertEquals("org.apache.sling.validation.impl.postprocessor.InvalidResourcePostProcessorException", error.getString("class"));
        assertEquals("Validation errors: field1 : Property does not match the pattern \"^\\p{Upper}+$\"., Missing required property with name \"field2\".", error.getString("message"));
    }
}
