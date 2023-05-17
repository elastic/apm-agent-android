/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoResource;
import co.elastic.apm.android.sdk.testutils.BaseConverterTest;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.resource.v1.Resource;

public class ProtoResourceConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversion() {
        Resource resource = Resource.newBuilder()
                .addAttributes(KeyValue.newBuilder().setKey("someKey").setValue(AnyValue.newBuilder().setStringValue("someValue").build()))
                .build();
        String schemaUrl = "someSchemaUrl";
        ProtoResource protoResource = new ProtoResource(resource, schemaUrl);

        io.opentelemetry.sdk.resources.Resource result = map(protoResource);

        assertEquals(schemaUrl, result.getSchemaUrl());
        Attributes attributes = result.getAttributes();
        assertEquals(1, attributes.size());
    }

    @Test
    public void verifyConversionWithoutSchemaUrl() {
        Resource resource = Resource.newBuilder()
                .addAttributes(KeyValue.newBuilder().setKey("someKey").setValue(AnyValue.newBuilder().setStringValue("someValue").build()))
                .build();
        ProtoResource protoResource = new ProtoResource(resource, null);

        io.opentelemetry.sdk.resources.Resource result = map(protoResource);

        assertNull(result.getSchemaUrl());
        Attributes attributes = result.getAttributes();
        assertEquals(1, attributes.size());
    }
}
