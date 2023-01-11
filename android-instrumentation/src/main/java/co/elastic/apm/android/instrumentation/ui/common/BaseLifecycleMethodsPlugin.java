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
package co.elastic.apm.android.instrumentation.ui.common;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import androidx.annotation.NonNull;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BaseLifecycleMethodsPlugin implements Plugin {
    private List<MethodIdentity> cachedTargetMethods = null;

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
                                        TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        AvailableMethods availableMethods = getAvailableLifecycleMethods(typeDescription);
        if (availableMethods == null) {
            // No Operation.
            return builder;
        }

        DynamicType.Builder<?> newBuilder = builder.visit(Advice.withCustomMapping().bind(IsLastLifecycleMethod.class, true).to(getAdviceClass()).on(getMethodMatcher(availableMethods.lastMethod)));

        if (!availableMethods.otherMethods.isEmpty()) {
            newBuilder = newBuilder.visit(Advice.withCustomMapping().bind(IsLastLifecycleMethod.class, false).to(getAdviceClass()).on(getMultipleMethodsMatcher(availableMethods.otherMethods)));
        }

        return newBuilder;
    }

    @NonNull
    protected abstract Class<?> getAdviceClass();

    /**
     * Must return the list of lifecycle target methods in the order they are supposed to be
     * called.
     */
    protected abstract List<MethodIdentity> provideOrderedTargetMethods();

    @Override
    public void close() {
        cachedTargetMethods = null;
    }

    private Junction<MethodDescription> getMultipleMethodsMatcher(List<MethodIdentity> methods) {
        Junction<MethodDescription> elementMatcher = null;
        for (MethodIdentity method : methods) {
            if (elementMatcher == null) {
                elementMatcher = getMethodMatcher(method);
            } else {
                elementMatcher = elementMatcher.or(getMethodMatcher(method));
            }
        }
        return elementMatcher;
    }

    @NonNull
    private Junction<MethodDescription> getMethodMatcher(MethodIdentity method) {
        return named(method.name).and(takesArguments(method.argumentTypes)).and(returns(method.returnType));
    }

    private AvailableMethods getAvailableLifecycleMethods(TypeDescription typeDescription) {
        int foundMethodsCount = 0;
        List<MethodIdentity> foundMethods = new ArrayList<>();
        List<MethodIdentity> targetMethods = getTargetMethods();
        int maxMethods = targetMethods.size();
        for (MethodDescription.InDefinedShape declaredMethod : typeDescription.getDeclaredMethods()) {
            MethodIdentity methodIdentity = convert(declaredMethod);
            if (targetMethods.contains(methodIdentity)) {
                foundMethods.add(methodIdentity);
                foundMethodsCount++;
            }
            if (foundMethodsCount == maxMethods) {
                break;
            }
        }

        if (foundMethods.isEmpty()) {
            // No lifecycle methods defined in the target class.
            return null;
        }

        AvailableMethods availableMethods = new AvailableMethods();

        // Looping backwards to get the last lifecycle methods first.
        for (int i = targetMethods.size() - 1; i >= 0; i--) {
            MethodIdentity method = targetMethods.get(i);
            if (foundMethods.contains(method)) {
                if (availableMethods.lastMethod == null) {
                    availableMethods.lastMethod = method;
                } else {
                    availableMethods.otherMethods.add(method);
                }
            }
        }

        return availableMethods;
    }

    private MethodIdentity convert(MethodDescription.InDefinedShape methodDescription) {
        List<TypeDescription> arguments = new ArrayList<>();
        for (ParameterDescription.InDefinedShape parameter : methodDescription.getParameters()) {
            arguments.add(parameter.getType().asErasure());
        }
        return new MethodIdentity(methodDescription.getName(), methodDescription.getReturnType().asErasure(), arguments);
    }

    private List<MethodIdentity> getTargetMethods() {
        if (cachedTargetMethods == null) {
            cachedTargetMethods = provideOrderedTargetMethods();
        }
        return cachedTargetMethods;
    }

    private static class AvailableMethods {
        private MethodIdentity lastMethod;
        private final List<MethodIdentity> otherMethods = new ArrayList<>();
    }

    protected static class MethodIdentity {
        public final String name;
        public final TypeDescription returnType;
        public final List<TypeDescription> argumentTypes;

        public static MethodIdentity create(String name, Class<?> returnType, Class<?>... argumentTypes) {
            List<TypeDescription> arguments = new ArrayList<>();
            for (Class<?> argumentType : argumentTypes) {
                arguments.add(TypeDescription.ForLoadedType.of(argumentType));
            }
            return new MethodIdentity(name, TypeDescription.ForLoadedType.of(returnType), arguments);
        }

        private MethodIdentity(String name, TypeDescription returnType, List<TypeDescription> argumentTypes) {
            this.name = name;
            this.returnType = returnType;
            this.argumentTypes = argumentTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodIdentity that = (MethodIdentity) o;
            return Objects.equals(name, that.name) && Objects.equals(returnType, that.returnType) && Objects.equals(argumentTypes, that.argumentTypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, returnType, argumentTypes);
        }
    }
}
