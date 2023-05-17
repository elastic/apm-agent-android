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

import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.testdata.ResourceGenerator;
import co.elastic.apm.android.sdk.testutils.BaseConverterTest;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.resources.Resource;

public class ResourceConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversion() {
        Resource resource = ResourceGenerator.singleAttributeResource("someAttr", "someValue");

        io.opentelemetry.proto.resource.v1.Resource result = map(resource);

        List<KeyValue> attributesList = result.getAttributesList();
        assertEquals(1, attributesList.size());
        KeyValue keyValue = attributesList.get(0);
        assertEquals("someAttr", keyValue.getKey());
        assertEquals("someValue", keyValue.getValue().getStringValue());
    }
}
