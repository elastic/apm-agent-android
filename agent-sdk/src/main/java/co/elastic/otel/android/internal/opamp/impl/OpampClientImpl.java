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
package co.elastic.otel.android.internal.opamp.impl;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import co.elastic.otel.android.internal.opamp.OpampClient;
import co.elastic.otel.android.internal.opamp.impl.recipe.AgentToServerAppenders;
import co.elastic.otel.android.internal.opamp.impl.recipe.RecipeManager;
import co.elastic.otel.android.internal.opamp.impl.recipe.RequestRecipe;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.AgentDescriptionAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.AgentDisconnectAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.CapabilitiesAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.EffectiveConfigAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.FlagsAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.InstanceUidAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.RemoteConfigStatusAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.SequenceNumberAppender;
import co.elastic.otel.android.internal.opamp.request.Request;
import co.elastic.otel.android.internal.opamp.request.service.RequestService;
import co.elastic.otel.android.internal.opamp.response.MessageData;
import co.elastic.otel.android.internal.opamp.response.Response;
import co.elastic.otel.android.internal.opamp.state.FieldType;
import co.elastic.otel.android.internal.opamp.state.State;
import okio.ByteString;
import opamp.proto.AgentDescription;
import opamp.proto.AgentToServer;
import opamp.proto.RemoteConfigStatus;
import opamp.proto.ServerErrorResponse;
import opamp.proto.ServerToAgent;
import opamp.proto.ServerToAgentFlags;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class OpampClientImpl
        implements OpampClient, State.Listener, RequestService.Callback, Supplier<Request> {
    private final RequestService requestService;
    private final AgentToServerAppenders appenders;
    private final OpampClientState state;
    private final RecipeManager recipeManager;
    private final Lock runningLock = new ReentrantLock();
    private Callbacks callbacks;
    private boolean isRunning;
    private boolean isStopped;

    /**
     * Fields that must always be sent.
     */
    private static final List<FieldType> CONSTANT_FIELDS =
            List.of(FieldType.INSTANCE_UID, FieldType.SEQUENCE_NUM, FieldType.CAPABILITIES);

    /**
     * Fields that should only be sent in the first message and then omitted in following messages,
     * unless their value changes or the server requests a full message.
     */
    private static final List<FieldType> COMPRESSABLE_FIELDS =
            List.of(
                    FieldType.AGENT_DESCRIPTION, FieldType.EFFECTIVE_CONFIG, FieldType.REMOTE_CONFIG_STATUS);

    public static OpampClientImpl create(RequestService requestService, OpampClientState state) {
        AgentToServerAppenders appenders =
                new AgentToServerAppenders(
                        AgentDescriptionAppender.create(state.agentDescription),
                        EffectiveConfigAppender.create(state.effectiveConfig),
                        RemoteConfigStatusAppender.create(state.remoteConfigStatus),
                        SequenceNumberAppender.create(state.sequenceNum),
                        CapabilitiesAppender.create(state.capabilities),
                        InstanceUidAppender.create(state.instanceUid),
                        FlagsAppender.create(state.flags),
                        AgentDisconnectAppender.create());
        RecipeManager recipeManager = new RecipeManager();
        recipeManager.setConstantFields(CONSTANT_FIELDS);
        return new OpampClientImpl(requestService, appenders, state, recipeManager);
    }

    private OpampClientImpl(
            RequestService requestService,
            AgentToServerAppenders appenders,
            OpampClientState state,
            RecipeManager recipeManager) {
        this.requestService = requestService;
        this.appenders = appenders;
        this.state = state;
        this.recipeManager = recipeManager;
    }

    @Override
    public void start(Callbacks callbacks) {
        runningLock.lock();
        try {
            if (!isRunning) {
                isRunning = true;
                this.callbacks = callbacks;
                observeStateChange();
                disableCompression();
                requestService.start(this, this);
                requestService.sendRequest();
            } else {
                throw new IllegalStateException("The client has already been started");
            }
        } finally {
            runningLock.unlock();
        }
    }

    @Override
    public void stop() {
        runningLock.lock();
        try {
            if (!isRunning) {
                throw new IllegalStateException("The client has not been started");
            }
            if (!isStopped) {
                isStopped = true;
                prepareDisconnectRequest();
                requestService.stop();
            } else {
                throw new IllegalStateException("The client has already been stopped");
            }
        } finally {
            runningLock.unlock();
        }
    }

    @Override
    public void setAgentDescription(AgentDescription agentDescription) {
        state.agentDescription.set(agentDescription);
    }

    @Override
    public void setRemoteConfigStatus(RemoteConfigStatus remoteConfigStatus) {
        state.remoteConfigStatus.set(remoteConfigStatus);
    }

    @Override
    public void onConnectionSuccess() {
        callbacks.onConnect(this);
    }

    @Override
    public void onConnectionFailed(Throwable throwable) {
        callbacks.onConnectFailed(this, throwable);
        preserveFailedRequestRecipe();
    }

    @Override
    public void onRequestSuccess(Response response) {
        state.sequenceNum.increment();
        if (response == null) return;

        handleResponsePayload(response.getServerToAgent());
    }

    @Override
    public void onRequestFailed(Throwable throwable) {
        preserveFailedRequestRecipe();
    }

    private void handleResponsePayload(ServerToAgent response) {
        if (response.error_response != null) {
            ServerErrorResponse errorResponse = response.error_response;
            callbacks.onErrorResponse(this, errorResponse);
        }
        int reportFullState = ServerToAgentFlags.ServerToAgentFlags_ReportFullState.getValue();
        if ((response.flags & reportFullState) == reportFullState) {
            disableCompression();
        }
        handleAgentIdentification(response);

        boolean notifyOnMessage = false;
        MessageData.Builder messageBuilder = MessageData.builder();

        if (response.remote_config != null) {
            notifyOnMessage = true;
            messageBuilder.setRemoteConfig(response.remote_config);
        }

        if (notifyOnMessage) {
            callbacks.onMessage(this, messageBuilder.build());
        }
    }

    private void handleAgentIdentification(ServerToAgent response) {
        if (response.agent_identification != null) {
            ByteString newInstanceUid = response.agent_identification.new_instance_uid;
            if (newInstanceUid.size() > 0) {
                state.instanceUid.set(newInstanceUid.toByteArray());
            }
        }
    }

    private void disableCompression() {
        recipeManager.next().addAllFields(COMPRESSABLE_FIELDS);
    }

    private void prepareDisconnectRequest() {
        recipeManager.next().addField(FieldType.AGENT_DISCONNECT);
    }

    private void preserveFailedRequestRecipe() {
        final RequestRecipe previous = recipeManager.previous();
        if (previous != null) {
            recipeManager.next().merge(previous);
        }
    }

    @Override
    public Request get() {
        AgentToServer.Builder builder = new AgentToServer.Builder();
        for (FieldType field : recipeManager.next().build().getFields()) {
            appenders.getForField(field).appendTo(builder);
        }
        return Request.create(builder.build());
    }

    private void observeStateChange() {
        state.agentDescription.addListener(this);
        state.effectiveConfig.addListener(this);
        state.remoteConfigStatus.addListener(this);
        state.capabilities.addListener(this);
        state.instanceUid.addListener(this);
    }

    @Override
    public void onUpdate(FieldType type) {
        recipeManager.next().addField(type);
        requestService.sendRequest();
    }
}
