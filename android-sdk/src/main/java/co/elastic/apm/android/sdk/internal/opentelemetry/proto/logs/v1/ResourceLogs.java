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
 * A collection of ScopeLogs from a Resource.
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.logs.v1.ResourceLogs}
 */
public  final class ResourceLogs extends
    com.google.protobuf.GeneratedMessageLite<
        ResourceLogs, ResourceLogs.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.logs.v1.ResourceLogs)
    ResourceLogsOrBuilder {
  private ResourceLogs() {
    scopeLogs_ = emptyProtobufList();
    schemaUrl_ = "";
  }
  public static final int RESOURCE_FIELD_NUMBER = 1;
  private co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource resource_;
  /**
   * <pre>
   * The resource for the logs in this message.
   * If this field is not set then resource info is unknown.
   * </pre>
   *
   * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
   */
  @java.lang.Override
  public boolean hasResource() {
    return resource_ != null;
  }
  /**
   * <pre>
   * The resource for the logs in this message.
   * If this field is not set then resource info is unknown.
   * </pre>
   *
   * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource getResource() {
    return resource_ == null ? co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource.getDefaultInstance() : resource_;
  }
  /**
   * <pre>
   * The resource for the logs in this message.
   * If this field is not set then resource info is unknown.
   * </pre>
   *
   * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
   */
  private void setResource(co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource value) {
    value.getClass();
  resource_ = value;
    
    }
  /**
   * <pre>
   * The resource for the logs in this message.
   * If this field is not set then resource info is unknown.
   * </pre>
   *
   * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeResource(co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource value) {
    value.getClass();
  if (resource_ != null &&
        resource_ != co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource.getDefaultInstance()) {
      resource_ =
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource.newBuilder(resource_).mergeFrom(value).buildPartial();
    } else {
      resource_ = value;
    }
    
  }
  /**
   * <pre>
   * The resource for the logs in this message.
   * If this field is not set then resource info is unknown.
   * </pre>
   *
   * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
   */
  private void clearResource() {  resource_ = null;
    
  }

  public static final int SCOPE_LOGS_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs> scopeLogs_;
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  @java.lang.Override
  public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs> getScopeLogsList() {
    return scopeLogs_;
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  public java.util.List<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogsOrBuilder> 
      getScopeLogsOrBuilderList() {
    return scopeLogs_;
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  @java.lang.Override
  public int getScopeLogsCount() {
    return scopeLogs_.size();
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs getScopeLogs(int index) {
    return scopeLogs_.get(index);
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogsOrBuilder getScopeLogsOrBuilder(
      int index) {
    return scopeLogs_.get(index);
  }
  private void ensureScopeLogsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs> tmp = scopeLogs_;
    if (!tmp.isModifiable()) {
      scopeLogs_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  private void setScopeLogs(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs value) {
    value.getClass();
  ensureScopeLogsIsMutable();
    scopeLogs_.set(index, value);
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  private void addScopeLogs(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs value) {
    value.getClass();
  ensureScopeLogsIsMutable();
    scopeLogs_.add(value);
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  private void addScopeLogs(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs value) {
    value.getClass();
  ensureScopeLogsIsMutable();
    scopeLogs_.add(index, value);
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  private void addAllScopeLogs(
      java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs> values) {
    ensureScopeLogsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, scopeLogs_);
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  private void clearScopeLogs() {
    scopeLogs_ = emptyProtobufList();
  }
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  private void removeScopeLogs(int index) {
    ensureScopeLogsIsMutable();
    scopeLogs_.remove(index);
  }

  public static final int SCHEMA_URL_FIELD_NUMBER = 3;
  private java.lang.String schemaUrl_;
  /**
   * <pre>
   * This schema_url applies to the data in the "resource" field. It does not apply
   * to the data in the "scope_logs" field which have their own schema_url field.
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
   * This schema_url applies to the data in the "resource" field. It does not apply
   * to the data in the "scope_logs" field which have their own schema_url field.
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
   * This schema_url applies to the data in the "resource" field. It does not apply
   * to the data in the "scope_logs" field which have their own schema_url field.
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
   * This schema_url applies to the data in the "resource" field. It does not apply
   * to the data in the "scope_logs" field which have their own schema_url field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   */
  private void clearSchemaUrl() {
    
    schemaUrl_ = getDefaultInstance().getSchemaUrl();
  }
  /**
   * <pre>
   * This schema_url applies to the data in the "resource" field. It does not apply
   * to the data in the "scope_logs" field which have their own schema_url field.
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

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * A collection of ScopeLogs from a Resource.
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.logs.v1.ResourceLogs}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.logs.v1.ResourceLogs)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogsOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * The resource for the logs in this message.
     * If this field is not set then resource info is unknown.
     * </pre>
     *
     * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
     */
    @java.lang.Override
    public boolean hasResource() {
      return instance.hasResource();
    }
    /**
     * <pre>
     * The resource for the logs in this message.
     * If this field is not set then resource info is unknown.
     * </pre>
     *
     * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource getResource() {
      return instance.getResource();
    }
    /**
     * <pre>
     * The resource for the logs in this message.
     * If this field is not set then resource info is unknown.
     * </pre>
     *
     * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
     */
    public Builder setResource(co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource value) {
      copyOnWrite();
      instance.setResource(value);
      return this;
      }
    /**
     * <pre>
     * The resource for the logs in this message.
     * If this field is not set then resource info is unknown.
     * </pre>
     *
     * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
     */
    public Builder setResource(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource.Builder builderForValue) {
      copyOnWrite();
      instance.setResource(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * The resource for the logs in this message.
     * If this field is not set then resource info is unknown.
     * </pre>
     *
     * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
     */
    public Builder mergeResource(co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource value) {
      copyOnWrite();
      instance.mergeResource(value);
      return this;
    }
    /**
     * <pre>
     * The resource for the logs in this message.
     * If this field is not set then resource info is unknown.
     * </pre>
     *
     * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
     */
    public Builder clearResource() {  copyOnWrite();
      instance.clearResource();
      return this;
    }

    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    @java.lang.Override
    public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs> getScopeLogsList() {
      return java.util.Collections.unmodifiableList(
          instance.getScopeLogsList());
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    @java.lang.Override
    public int getScopeLogsCount() {
      return instance.getScopeLogsCount();
    }/**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs getScopeLogs(int index) {
      return instance.getScopeLogs(index);
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder setScopeLogs(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs value) {
      copyOnWrite();
      instance.setScopeLogs(index, value);
      return this;
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder setScopeLogs(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs.Builder builderForValue) {
      copyOnWrite();
      instance.setScopeLogs(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder addScopeLogs(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs value) {
      copyOnWrite();
      instance.addScopeLogs(value);
      return this;
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder addScopeLogs(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs value) {
      copyOnWrite();
      instance.addScopeLogs(index, value);
      return this;
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder addScopeLogs(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs.Builder builderForValue) {
      copyOnWrite();
      instance.addScopeLogs(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder addScopeLogs(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs.Builder builderForValue) {
      copyOnWrite();
      instance.addScopeLogs(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder addAllScopeLogs(
        java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs> values) {
      copyOnWrite();
      instance.addAllScopeLogs(values);
      return this;
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder clearScopeLogs() {
      copyOnWrite();
      instance.clearScopeLogs();
      return this;
    }
    /**
     * <pre>
     * A list of ScopeLogs that originate from a resource.
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
     */
    public Builder removeScopeLogs(int index) {
      copyOnWrite();
      instance.removeScopeLogs(index);
      return this;
    }

    /**
     * <pre>
     * This schema_url applies to the data in the "resource" field. It does not apply
     * to the data in the "scope_logs" field which have their own schema_url field.
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
     * This schema_url applies to the data in the "resource" field. It does not apply
     * to the data in the "scope_logs" field which have their own schema_url field.
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
     * This schema_url applies to the data in the "resource" field. It does not apply
     * to the data in the "scope_logs" field which have their own schema_url field.
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
     * This schema_url applies to the data in the "resource" field. It does not apply
     * to the data in the "scope_logs" field which have their own schema_url field.
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
     * This schema_url applies to the data in the "resource" field. It does not apply
     * to the data in the "scope_logs" field which have their own schema_url field.
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

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.logs.v1.ResourceLogs)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "resource_",
            "scopeLogs_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs.class,
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
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.logs.v1.ResourceLogs)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs DEFAULT_INSTANCE;
  static {
    ResourceLogs defaultInstance = new ResourceLogs();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ResourceLogs.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ResourceLogs> PARSER;

  public static com.google.protobuf.Parser<ResourceLogs> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

