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

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoAttributes;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoResource;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Converter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Mapper;
import io.opentelemetry.sdk.resources.Resource;

public class ProtoResourceConverter extends Converter<ProtoResource, Resource> {

    @Override
    protected Resource doConvert(Mapper mapper, ProtoResource from) {
        ProtoAttributes attributes = new ProtoAttributes(from.resource.getAttributesList());
        return Resource.create(mapper.map(attributes), from.schemaUrl);
    }
}
