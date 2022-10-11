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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.bytebuddy.build.AndroidDescriptor;
import net.bytebuddy.description.type.TypeDescription;

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.instrumentation.ui.common.BaseLifecycleMethodsPlugin;

public class FragmentLifecyclePlugin extends BaseLifecycleMethodsPlugin {
    private final AndroidDescriptor androidDescriptor;

    public FragmentLifecyclePlugin(AndroidDescriptor androidDescriptor) {
        this.androidDescriptor = androidDescriptor;
    }

    @NonNull
    @Override
    protected Class<?> getAdviceClass() {
        return FragmentLifecycleMethodAdvice.class;
    }

    @Override
    protected List<MethodIdentity> provideOrderedTargetMethods() {
        List<MethodIdentity> methods = new ArrayList<>();
        methods.add(MethodIdentity.create("onCreate", void.class, Bundle.class));
        methods.add(MethodIdentity.create("onCreateView", View.class, LayoutInflater.class, ViewGroup.class, Bundle.class));
        methods.add(MethodIdentity.create("onViewCreated", void.class, View.class, Bundle.class));
        return methods;
    }

    @Override
    public boolean matches(TypeDescription target) {
        if (androidDescriptor.getTypeScope(target) == AndroidDescriptor.TypeScope.EXTERNAL) {
            return false;
        }
        return !target.getSimpleName().startsWith("Hilt_") && target.isAssignableTo(Fragment.class);
    }
}
