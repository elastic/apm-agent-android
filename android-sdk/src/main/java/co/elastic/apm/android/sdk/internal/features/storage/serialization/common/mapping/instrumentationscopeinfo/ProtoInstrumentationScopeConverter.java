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

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoAttributes;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoInstrumentationScope;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Converter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Mapper;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;

public class ProtoInstrumentationScopeConverter extends Converter<ProtoInstrumentationScope, InstrumentationScopeInfo> {

    @Override
    protected InstrumentationScopeInfo doConvert(Mapper mapper, ProtoInstrumentationScope from) {
        InstrumentationScope scope = from.scope;
        ProtoAttributes protoAttributes = new ProtoAttributes(scope.getAttributesList());
        return InstrumentationScopeInfo.builder(scope.getName())
                .setVersion(scope.getVersion())
                .setAttributes(mapper.map(protoAttributes))
                .setSchemaUrl(from.schemaUrl)
                .build();
    }
}
