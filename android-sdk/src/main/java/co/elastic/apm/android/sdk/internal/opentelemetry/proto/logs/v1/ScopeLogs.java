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
 * A collection of Logs produced by a Scope.
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.logs.v1.ScopeLogs}
 */
public  final class ScopeLogs extends
    com.google.protobuf.GeneratedMessageLite<
        ScopeLogs, ScopeLogs.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.logs.v1.ScopeLogs)
    ScopeLogsOrBuilder {
  private ScopeLogs() {
    logRecords_ = emptyProtobufList();
    schemaUrl_ = "";
  }
  public static final int SCOPE_FIELD_NUMBER = 1;
  private co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope scope_;
  /**
   * <pre>
   * The instrumentation scope information for the logs in this message.
   * Semantically when InstrumentationScope isn't set, it is equivalent with
   * an empty instrumentation scope name (unknown).
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
   */
  @java.lang.Override
  public boolean hasScope() {
    return scope_ != null;
  }
  /**
   * <pre>
   * The instrumentation scope information for the logs in this message.
   * Semantically when InstrumentationScope isn't set, it is equivalent with
   * an empty instrumentation scope name (unknown).
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope getScope() {
    return scope_ == null ? co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope.getDefaultInstance() : scope_;
  }
  /**
   * <pre>
   * The instrumentation scope information for the logs in this message.
   * Semantically when InstrumentationScope isn't set, it is equivalent with
   * an empty instrumentation scope name (unknown).
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
   */
  private void setScope(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope value) {
    value.getClass();
  scope_ = value;
    
    }
  /**
   * <pre>
   * The instrumentation scope information for the logs in this message.
   * Semantically when InstrumentationScope isn't set, it is equivalent with
   * an empty instrumentation scope name (unknown).
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeScope(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope value) {
    value.getClass();
  if (scope_ != null &&
        scope_ != co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope.getDefaultInstance()) {
      scope_ =
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope.newBuilder(scope_).mergeFrom(value).buildPartial();
    } else {
      scope_ = value;
    }
    
  }
  /**
   * <pre>
   * The instrumentation scope information for the logs in this message.
   * Semantically when InstrumentationScope isn't set, it is equivalent with
   * an empty instrumentation scope name (unknown).
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
   */
  private void clearScope() {  scope_ = null;
    
  }

  public static final int LOG_RECORDS_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord> logRecords_;
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  @java.lang.Override
  public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord> getLogRecordsList() {
    return logRecords_;
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  public java.util.List<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecordOrBuilder> 
      getLogRecordsOrBuilderList() {
    return logRecords_;
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  @java.lang.Override
  public int getLogRecordsCount() {
    return logRecords_.size();
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord getLogRecords(int index) {
    return logRecords_.get(index);
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecordOrBuilder getLogRecordsOrBuilder(
      int index) {
    return logRecords_.get(index);
  }
  private void ensureLogRecordsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord> tmp = logRecords_;
    if (!tmp.isModifiable()) {
      logRecords_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  private void setLogRecords(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord value) {
    value.getClass();
  ensureLogRecordsIsMutable();
    logRecords_.set(index, value);
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  private void addLogRecords(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord value) {
    value.getClass();
  ensureLogRecordsIsMutable();
    logRecords_.add(value);
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  private void addLogRecords(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord value) {
    value.getClass();
  ensureLogRecordsIsMutable();
    logRecords_.add(index, value);
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  private void addAllLogRecords(
      java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord> values) {
    ensureLogRecordsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, logRecords_);
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  private void clearLogRecords() {
    logRecords_ = emptyProtobufList();
  }
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  private void removeLogRecords(int index) {
    ensureLogRecordsIsMutable();
    logRecords_.remove(index);
  }

  public static final int SCHEMA_URL_FIELD_NUMBER = 3;
  private java.lang.String schemaUrl_;
  /**
   * <pre>
   * This schema_url applies to all logs in the "logs" field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   * @return The schemaUrl.
   */
  @java.lang.Override
  public java.lang.String getSchemaUrl() {
    return schemaUrl_;
  }
  /**
   * <pre>
   * This schema_url applies to all logs in the "logs" field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   * @return The bytes for schemaUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSchemaUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(schemaUrl_);
  }
  /**
   * <pre>
   * This schema_url applies to all logs in the "logs" field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   * @param value The schemaUrl to set.
   */
  private void setSchemaUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    schemaUrl_ = value;
  }
  /**
   * <pre>
   * This schema_url applies to all logs in the "logs" field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   */
  private void clearSchemaUrl() {
    
    schemaUrl_ = getDefaultInstance().getSchemaUrl();
  }
  /**
   * <pre>
   * This schema_url applies to all logs in the "logs" field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   * @param value The bytes for schemaUrl to set.
   */
  private void setSchemaUrlBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    schemaUrl_ = value.toStringUtf8();
    
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * A collection of Logs produced by a Scope.
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.logs.v1.ScopeLogs}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.logs.v1.ScopeLogs)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogsOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * The instrumentation scope information for the logs in this message.
     * Semantically when InstrumentationScope isn't set, it is equivalent with
     * an empty instrumentation scope name (unknown).
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
     */
    @java.lang.Override
    public boolean hasScope() {
      return instance.hasScope();
    }
    /**
     * <pre>
     * The instrumentation scope information for the logs in this message.
     * Semantically when InstrumentationScope isn't set, it is equivalent with
     * an empty instrumentation scope name (unknown).
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope getScope() {
      return instance.getScope();
    }
    /**
     * <pre>
     * The instrumentation scope information for the logs in this message.
     * Semantically when InstrumentationScope isn't set, it is equivalent with
     * an empty instrumentation scope name (unknown).
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
     */
    public Builder setScope(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope value) {
      copyOnWrite();
      instance.setScope(value);
      return this;
      }
    /**
     * <pre>
     * The instrumentation scope information for the logs in this message.
     * Semantically when InstrumentationScope isn't set, it is equivalent with
     * an empty instrumentation scope name (unknown).
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
     */
    public Builder setScope(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope.Builder builderForValue) {
      copyOnWrite();
      instance.setScope(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * The instrumentation scope information for the logs in this message.
     * Semantically when InstrumentationScope isn't set, it is equivalent with
     * an empty instrumentation scope name (unknown).
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
     */
    public Builder mergeScope(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope value) {
      copyOnWrite();
      instance.mergeScope(value);
      return this;
    }
    /**
     * <pre>
     * The instrumentation scope information for the logs in this message.
     * Semantically when InstrumentationScope isn't set, it is equivalent with
     * an empty instrumentation scope name (unknown).
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
     */
    public Builder clearScope() {  copyOnWrite();
      instance.clearScope();
      return this;
    }

    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    @java.lang.Override
    public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord> getLogRecordsList() {
      return java.util.Collections.unmodifiableList(
          instance.getLogRecordsList());
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    @java.lang.Override
    public int getLogRecordsCount() {
      return instance.getLogRecordsCount();
    }/**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord getLogRecords(int index) {
      return instance.getLogRecords(index);
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder setLogRecords(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord value) {
      copyOnWrite();
      instance.setLogRecords(index, value);
      return this;
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder setLogRecords(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord.Builder builderForValue) {
      copyOnWrite();
      instance.setLogRecords(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder addLogRecords(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord value) {
      copyOnWrite();
      instance.addLogRecords(value);
      return this;
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder addLogRecords(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord value) {
      copyOnWrite();
      instance.addLogRecords(index, value);
      return this;
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder addLogRecords(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord.Builder builderForValue) {
      copyOnWrite();
      instance.addLogRecords(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder addLogRecords(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord.Builder builderForValue) {
      copyOnWrite();
      instance.addLogRecords(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder addAllLogRecords(
        java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord> values) {
      copyOnWrite();
      instance.addAllLogRecords(values);
      return this;
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder clearLogRecords() {
      copyOnWrite();
      instance.clearLogRecords();
      return this;
    }
    /**
     * <pre>
     * A list of log records.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
     */
    public Builder removeLogRecords(int index) {
      copyOnWrite();
      instance.removeLogRecords(index);
      return this;
    }

    /**
     * <pre>
     * This schema_url applies to all logs in the "logs" field.
     * </pre>
     *
     * <code>string schema_url = 3;</code>
     * @return The schemaUrl.
     */
    @java.lang.Override
    public java.lang.String getSchemaUrl() {
      return instance.getSchemaUrl();
    }
    /**
     * <pre>
     * This schema_url applies to all logs in the "logs" field.
     * </pre>
     *
     * <code>string schema_url = 3;</code>
     * @return The bytes for schemaUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSchemaUrlBytes() {
      return instance.getSchemaUrlBytes();
    }
    /**
     * <pre>
     * This schema_url applies to all logs in the "logs" field.
     * </pre>
     *
     * <code>string schema_url = 3;</code>
     * @param value The schemaUrl to set.
     * @return This builder for chaining.
     */
    public Builder setSchemaUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setSchemaUrl(value);
      return this;
    }
    /**
     * <pre>
     * This schema_url applies to all logs in the "logs" field.
     * </pre>
     *
     * <code>string schema_url = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearSchemaUrl() {
      copyOnWrite();
      instance.clearSchemaUrl();
      return this;
    }
    /**
     * <pre>
     * This schema_url applies to all logs in the "logs" field.
     * </pre>
     *
     * <code>string schema_url = 3;</code>
     * @param value The bytes for schemaUrl to set.
     * @return This builder for chaining.
     */
    public Builder setSchemaUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSchemaUrlBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.logs.v1.ScopeLogs)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "scope_",
            "logRecords_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord.class,
            "schemaUrl_",
          };
          java.lang.String info =
              "\u0000\u0003\u0000\u0000\u0001\u0003\u0003\u0000\u0001\u0000\u0001\t\u0002\u001b" +
              "\u0003\u0208";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.logs.v1.ScopeLogs)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs DEFAULT_INSTANCE;
  static {
    ScopeLogs defaultInstance = new ScopeLogs();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ScopeLogs.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ScopeLogs> PARSER;

  public static com.google.protobuf.Parser<ScopeLogs> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

