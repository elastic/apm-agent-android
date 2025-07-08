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
package co.elastic.otel.android.internal.opamp.impl.recipe;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import co.elastic.otel.android.internal.opamp.request.Field;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RecipeManager {
    private final Object recipeLock = new Object();
    private final List<Field> constantFields;
    private RequestRecipe previousRecipe = null;
    private RecipeBuilder builder;

    public static RecipeManager create(List<Field> constantFields) {
        return new RecipeManager(Collections.unmodifiableList(constantFields));
    }

    private RecipeManager(List<Field> constantFields) {
        this.constantFields = constantFields;
    }

    public RequestRecipe previous() {
        synchronized (recipeLock) {
            return previousRecipe;
        }
    }

    @Nonnull
    public RecipeBuilder next() {
        synchronized (recipeLock) {
            if (builder == null) {
                builder = new RecipeBuilder(constantFields);
            }
            return builder;
        }
    }

    public final class RecipeBuilder {
        private final Set<Field> fields = new HashSet<>();

        public RecipeBuilder addField(Field field) {
            fields.add(field);
            return this;
        }

        public RecipeBuilder addAllFields(Collection<Field> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public RecipeBuilder merge(RequestRecipe recipe) {
            return addAllFields(recipe.getFields());
        }

        public RequestRecipe build() {
            synchronized (recipeLock) {
                RequestRecipe recipe = new RequestRecipe(Collections.unmodifiableCollection(fields));
                previousRecipe = recipe;
                builder = null;
                return recipe;
            }
        }

        private RecipeBuilder(List<Field> initialFields) {
            fields.addAll(initialFields);
        }
    }
}
