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
package co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1;

/**
 * <pre>
 * LogsData represents the logs data that can be stored in a persistent storage,
 * OR can be embedded by other protocols that transfer OTLP logs data but do not
 * implement the OTLP protocol.
 * The main difference between this message and collector protocol is that
 * in this message there will not be any "control" or "metadata" specific to
 * OTLP protocol.
 * When new fields are added into this message, the OTLP request MUST be updated
 * as well.
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.logs.v1.LogsData}
 */
public  final class LogsData extends
    com.google.protobuf.GeneratedMessageLite<
        LogsData, LogsData.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.logs.v1.LogsData)
    LogsDataOrBuilder {
  private LogsData() {
    resourceLogs_ = emptyProtobufList();
  }
  public static final int RESOURCE_LOGS_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs> resourceLogs_;
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  @java.lang.Override
  public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs> getResourceLogsList() {
    return resourceLogs_;
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  public java.util.List<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogsOrBuilder> 
      getResourceLogsOrBuilderList() {
    return resourceLogs_;
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  @java.lang.Override
  public int getResourceLogsCount() {
    return resourceLogs_.size();
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs getResourceLogs(int index) {
    return resourceLogs_.get(index);
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogsOrBuilder getResourceLogsOrBuilder(
      int index) {
    return resourceLogs_.get(index);
  }
  private void ensureResourceLogsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs> tmp = resourceLogs_;
    if (!tmp.isModifiable()) {
      resourceLogs_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  private void setResourceLogs(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs value) {
    value.getClass();
  ensureResourceLogsIsMutable();
    resourceLogs_.set(index, value);
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  private void addResourceLogs(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs value) {
    value.getClass();
  ensureResourceLogsIsMutable();
    resourceLogs_.add(value);
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  private void addResourceLogs(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs value) {
    value.getClass();
  ensureResourceLogsIsMutable();
    resourceLogs_.add(index, value);
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  private void addAllResourceLogs(
      java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs> values) {
    ensureResourceLogsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, resourceLogs_);
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  private void clearResourceLogs() {
    resourceLogs_ = emptyProtobufList();
  }
  /**
   * <pre>
   * An array of ResourceLogs.
   * For data coming from a single resource this array will typically contain
   * one element. Intermediary nodes that receive data from multiple origins
   * typically batch the data before forwarding further and in that case this
   * array will contain multiple elements.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
   */
  private void removeResourceLogs(int index) {
    ensureResourceLogsIsMutable();
    resourceLogs_.remove(index);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * LogsData represents the logs data that can be stored in a persistent storage,
   * OR can be embedded by other protocols that transfer OTLP logs data but do not
   * implement the OTLP protocol.
   * The main difference between this message and collector protocol is that
   * in this message there will not be any "control" or "metadata" specific to
   * OTLP protocol.
   * When new fields are added into this message, the OTLP request MUST be updated
   * as well.
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.logs.v1.LogsData}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.logs.v1.LogsData)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsDataOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    @java.lang.Override
    public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs> getResourceLogsList() {
      return java.util.Collections.unmodifiableList(
          instance.getResourceLogsList());
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    @java.lang.Override
    public int getResourceLogsCount() {
      return instance.getResourceLogsCount();
    }/**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs getResourceLogs(int index) {
      return instance.getResourceLogs(index);
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder setResourceLogs(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs value) {
      copyOnWrite();
      instance.setResourceLogs(index, value);
      return this;
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder setResourceLogs(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs.Builder builderForValue) {
      copyOnWrite();
      instance.setResourceLogs(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder addResourceLogs(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs value) {
      copyOnWrite();
      instance.addResourceLogs(value);
      return this;
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder addResourceLogs(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs value) {
      copyOnWrite();
      instance.addResourceLogs(index, value);
      return this;
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder addResourceLogs(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs.Builder builderForValue) {
      copyOnWrite();
      instance.addResourceLogs(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder addResourceLogs(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs.Builder builderForValue) {
      copyOnWrite();
      instance.addResourceLogs(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder addAllResourceLogs(
        java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs> values) {
      copyOnWrite();
      instance.addAllResourceLogs(values);
      return this;
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder clearResourceLogs() {
      copyOnWrite();
      instance.clearResourceLogs();
      return this;
    }
    /**
     * <pre>
     * An array of ResourceLogs.
     * For data coming from a single resource this array will typically contain
     * one element. Intermediary nodes that receive data from multiple origins
     * typically batch the data before forwarding further and in that case this
     * array will contain multiple elements.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ResourceLogs resource_logs = 1;</code>
     */
    public Builder removeResourceLogs(int index) {
      copyOnWrite();
      instance.removeResourceLogs(index);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.logs.v1.LogsData)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "resourceLogs_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs.class,
          };
          java.lang.String info =
              "\u0000\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0001\u0000\u0001\u001b";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData>(
                      DEFAULT_INSTANCE);
              PARSER = parser;
            }
          }
        }
        return parser;
    }
    case GET_MEMOIZED_IS_INITIALIZED: {
      return (byte) 1;
    }
    case SET_MEMOIZED_IS_INITIALIZED: {
      return null;
    }
    }
    throw new UnsupportedOperationException();
  }


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.logs.v1.LogsData)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData DEFAULT_INSTANCE;
  static {
    LogsData defaultInstance = new LogsData();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      LogsData.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<LogsData> PARSER;

  public static com.google.protobuf.Parser<LogsData> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

