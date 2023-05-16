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
package co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.instrumentationscopeinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoInstrumentationScope;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue;
import co.elastic.apm.android.sdk.testutils.BaseConverterTest;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;

public class ProtoInstrumentationScopeConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversion() {
        KeyValue attribute = KeyValue.newBuilder().setKey("someKey").setValue(AnyValue.newBuilder().setStringValue("someValue").build()).build();
        InstrumentationScope scope = InstrumentationScope.newBuilder()
                .addAttributes(attribute)
                .setName("someName")
                .setVersion("1.2.3")
                .build();
        String schemaUrl = "someSchemaUrl";

        ProtoInstrumentationScope protoInstrumentationScope = new ProtoInstrumentationScope(scope, schemaUrl);

        InstrumentationScopeInfo scopeInfo = map(protoInstrumentationScope);

        assertEquals("someName", scopeInfo.getName());
        assertEquals("1.2.3", scopeInfo.getVersion());
        Attributes attributes = scopeInfo.getAttributes();
        assertEquals(1, attributes.size());
        assertEquals(schemaUrl, scopeInfo.getSchemaUrl());
    }

    @Test
    public void verifyConversionWithoutSchemaUrl() {
        KeyValue attribute = KeyValue.newBuilder().setKey("someKey").setValue(AnyValue.newBuilder().setStringValue("someValue").build()).build();
        InstrumentationScope scope = InstrumentationScope.newBuilder()
                .addAttributes(attribute)
                .setName("someName")
                .setVersion("1.2.3")
                .build();

        ProtoInstrumentationScope protoInstrumentationScope = new ProtoInstrumentationScope(scope, null);

        InstrumentationScopeInfo scopeInfo = map(protoInstrumentationScope);

        assertEquals("someName", scopeInfo.getName());
        assertEquals("1.2.3", scopeInfo.getVersion());
        Attributes attributes = scopeInfo.getAttributes();
        assertEquals(1, attributes.size());
        assertNull(scopeInfo.getSchemaUrl());
    }
}
