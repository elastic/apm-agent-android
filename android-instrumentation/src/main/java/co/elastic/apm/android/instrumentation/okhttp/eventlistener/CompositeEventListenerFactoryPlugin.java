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
package co.elastic.apm.android.instrumentation.okhttp.eventlistener;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

import java.io.IOException;

import co.elastic.apm.android.common.internal.logging.Elog;

public class CompositeEventListenerFactoryPlugin implements Plugin {

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        Elog.getLogger().debug("Remapping to generated CompositeEventListener");
        return builder.visit(new CompositeEventListenerRemapper());
    }

    @Override
    public void close() throws IOException {
        // No operation.
    }

    @Override
    public boolean matches(TypeDescription target) {
        return target.getTypeName().equals("co.elastic.apm.android.common.okhttp.eventlistener.CompositeEventListenerFactory");
    }
}
