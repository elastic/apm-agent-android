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
package co.elastic.otel.android.okhttp.internal.callback;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Callback;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class OkHttpCallbackPlugin implements Plugin {
    private static final Pattern REAL_CALL_PATTERN = Pattern.compile("^okhttp3\\..*RealCall$");

    @Override
    public DynamicType.Builder<?> apply(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassFileLocator classFileLocator) {
        return builder.visit(
                Advice.to(OkHttpCallbackAdvice.class)
                        .on(named("enqueue").and(takesArgument(0, Callback.class))));
    }

    @Override
    public void close() throws IOException {
        // No operation.
    }

    @Override
    public boolean matches(TypeDescription target) {
        return REAL_CALL_PATTERN.matcher(target.getTypeName()).matches();
    }
}
