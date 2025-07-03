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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import co.elastic.otel.android.internal.opamp.state.FieldType;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RecipeManager {
    private final Lock previousRecipeLock = new ReentrantLock();
    private final Lock recipeBuilderLock = new ReentrantLock();
    private List<FieldType> constantFields = new ArrayList<>();
    private RequestRecipe previousRecipe = null;
    private RecipeBuilder builder;

    public RequestRecipe previous() {
        previousRecipeLock.lock();
        try {
            return previousRecipe;
        } finally {
            previousRecipeLock.unlock();
        }
    }

    public RecipeBuilder next() {
        recipeBuilderLock.lock();
        try {
            if (builder == null) {
                builder = new RecipeBuilder(constantFields);
            }
            return builder;
        } finally {
            recipeBuilderLock.unlock();
        }
    }

    private void setPreviousRecipe(RequestRecipe recipe) {
        previousRecipeLock.lock();
        try {
            this.previousRecipe = recipe;
        } finally {
            previousRecipeLock.unlock();
        }
    }

    private void clearBuilder() {
        builder = null;
    }

    public void setConstantFields(List<FieldType> constantFields) {
        this.constantFields = Collections.unmodifiableList(constantFields);
    }

    public final class RecipeBuilder {
        private final Set<FieldType> fields = new HashSet<>();

        public RecipeBuilder addField(FieldType field) {
            fields.add(field);
            return this;
        }

        public RecipeBuilder addAllFields(Collection<FieldType> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public RecipeBuilder merge(RequestRecipe recipe) {
            return addAllFields(recipe.getFields());
        }

        public RequestRecipe build() {
            recipeBuilderLock.lock();
            try {
                RequestRecipe recipe = new RequestRecipe(Collections.unmodifiableCollection(fields));
                setPreviousRecipe(recipe);
                clearBuilder();
                return recipe;
            } finally {
                recipeBuilderLock.unlock();
            }
        }

        private RecipeBuilder(List<FieldType> initialFields) {
            fields.addAll(initialFields);
        }
    }
}
