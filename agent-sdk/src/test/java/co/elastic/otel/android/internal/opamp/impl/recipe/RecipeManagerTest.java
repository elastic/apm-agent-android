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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import co.elastic.otel.android.internal.opamp.request.Field;

class RecipeManagerTest {

    @Test
    void verifyConstantValues() {
        RecipeManager recipeManager = RecipeManager.create(List.of(Field.AGENT_DESCRIPTION, Field.FLAGS));

        // First run
        assertThat(recipeManager.next().build().getFields()).containsExactlyInAnyOrder(Field.AGENT_DESCRIPTION, Field.FLAGS);

        // Adding extra fields
        recipeManager.next().addField(Field.CAPABILITIES);

        assertThat(recipeManager.next().build().getFields()).containsExactlyInAnyOrder(
                Field.AGENT_DESCRIPTION, Field.FLAGS, Field.CAPABILITIES
        );

        // Not adding fields for the next build
        assertThat(recipeManager.next().build().getFields()).containsExactlyInAnyOrder(Field.AGENT_DESCRIPTION, Field.FLAGS);
    }

    @Test
    void verifyPreviousFields() {
        RecipeManager recipeManager = RecipeManager.create(List.of(Field.CAPABILITIES, Field.FLAGS));

        // Previous build when there's none
        assertThat(recipeManager.previous()).isNull();

        // First build
        Collection<Field> fields = recipeManager.next()
                .addField(Field.REMOTE_CONFIG_STATUS)
                .build().getFields();
        assertThat(fields).containsExactlyInAnyOrder(Field.CAPABILITIES, Field.FLAGS, Field.REMOTE_CONFIG_STATUS);
        assertThat(recipeManager.previous().getFields()).isEqualTo(fields);

        // Merging fields
        recipeManager.next().addField(Field.AGENT_DISCONNECT)
                .merge(recipeManager.previous());
        assertThat(recipeManager.next().build().getFields()).containsExactlyInAnyOrder(
                Field.CAPABILITIES, Field.FLAGS, Field.REMOTE_CONFIG_STATUS, Field.AGENT_DISCONNECT
        );
    }
}