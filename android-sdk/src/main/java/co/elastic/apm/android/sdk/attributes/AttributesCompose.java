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
package co.elastic.apm.android.sdk.attributes;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.elastic.apm.android.sdk.traces.common.attributes.DeviceIdVisitor;
import co.elastic.apm.android.sdk.traces.common.attributes.DeviceInfoVisitor;
import co.elastic.apm.android.sdk.traces.common.attributes.OsDescriptorVisitor;
import co.elastic.apm.android.sdk.traces.common.attributes.RuntimeDescriptorVisitor;
import co.elastic.apm.android.sdk.traces.common.attributes.SdkIdVisitor;
import co.elastic.apm.android.sdk.traces.common.attributes.ServiceIdVisitor;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;

public final class AttributesCompose {
    private final List<AttributesVisitor> visitors = new ArrayList<>();

    public static AttributesCompose create(AttributesVisitor... visitors) {
        return new AttributesCompose(Arrays.asList(visitors));
    }

    public static AttributesCompose global(Context appContext,
                                           @Nullable String serviceName,
                                           @Nullable String serviceVersion) {
        return create(new DeviceIdVisitor(appContext),
                new DeviceInfoVisitor(),
                new OsDescriptorVisitor(),
                new RuntimeDescriptorVisitor(),
                new SdkIdVisitor(),
                new ServiceIdVisitor(serviceName, serviceVersion));
    }

    public AttributesCompose(List<AttributesVisitor> defaultVisitors) {
        if (defaultVisitors != null) {
            visitors.addAll(defaultVisitors);
        }
    }

    public void addVisitor(AttributesVisitor visitor) {
        if (visitor == null) {
            throw new NullPointerException();
        }
        visitors.add(visitor);
    }

    public Attributes provide() {
        AttributesBuilder builder = Attributes.builder();

        for (AttributesVisitor visitor : visitors) {
            visitor.visit(builder);
        }

        return builder.build();
    }

    public Resource provideAsResource() {
        return Resource.create(provide());
    }
}
