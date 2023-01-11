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
package co.elastic.apm.android.instrumentation.ui.fragments;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.util.Objects;

import co.elastic.apm.android.instrumentation.ui.common.IsLastLifecycleMethod;
import co.elastic.apm.android.sdk.internal.instrumentation.LifecycleMultiMethodSpan;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;

public class FragmentLifecycleMethodAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter(
            @Advice.Origin("#t") String ownerName,
            @Advice.Origin("#m") String methodName,
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope) {
        spanWithScope = LifecycleMultiMethodSpan.onMethodEnter(ownerName, methodName, ElasticTracer.androidFragment());
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.Origin("#t") String ownerName,
            @Advice.Origin("#r") String returnType,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returned,
            @Advice.Local("elasticSpanWithScope") LifecycleMultiMethodSpan.SpanWithScope spanWithScope,
            @IsLastLifecycleMethod boolean isLastMethod,
            @Advice.Thrown Throwable thrown) {
        boolean endRoot = false;
        if (!Objects.equals(returnType, "void")) {
            endRoot = returned == null;
        }
        LifecycleMultiMethodSpan.onMethodExit(ownerName, spanWithScope, thrown, endRoot || isLastMethod);
    }
}
