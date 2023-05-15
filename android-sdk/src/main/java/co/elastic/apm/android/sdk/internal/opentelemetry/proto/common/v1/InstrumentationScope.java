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
package co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1;

/**
 * <pre>
 * InstrumentationScope is a message representing the instrumentation scope information
 * such as the fully qualified name and version. 
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.common.v1.InstrumentationScope}
 */
public  final class InstrumentationScope extends
    com.google.protobuf.GeneratedMessageLite<
        InstrumentationScope, InstrumentationScope.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.common.v1.InstrumentationScope)
    InstrumentationScopeOrBuilder {
  private InstrumentationScope() {
    name_ = "";
    version_ = "";
    attributes_ = emptyProtobufList();
  }
  public static final int NAME_FIELD_NUMBER = 1;
  private java.lang.String name_;
  /**
   * <pre>
   * An empty instrumentation scope name means the name is unknown.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The name.
   */
  @java.lang.Override
  public java.lang.String getName() {
    return name_;
  }
  /**
   * <pre>
   * An empty instrumentation scope name means the name is unknown.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(name_);
  }
  /**
   * <pre>
   * An empty instrumentation scope name means the name is unknown.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @param value The name to set.
   */
  private void setName(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    name_ = value;
  }
  /**
   * <pre>
   * An empty instrumentation scope name means the name is unknown.
   * </pre>
   *
   * <code>string name = 1;</code>
   */
  private void clearName() {
    
    name_ = getDefaultInstance().getName();
  }
  /**
   * <pre>
   * An empty instrumentation scope name means the name is unknown.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @param value The bytes for name to set.
   */
  private void setNameBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    name_ = value.toStringUtf8();
    
  }

  public static final int VERSION_FIELD_NUMBER = 2;
  private java.lang.String version_;
  /**
   * <code>string version = 2;</code>
   * @return The version.
   */
  @java.lang.Override
  public java.lang.String getVersion() {
    return version_;
  }
  /**
   * <code>string version = 2;</code>
   * @return The bytes for version.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getVersionBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(version_);
  }
  /**
   * <code>string version = 2;</code>
   * @param value The version to set.
   */
  private void setVersion(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    version_ = value;
  }
  /**
   * <code>string version = 2;</code>
   */
  private void clearVersion() {
    
    version_ = getDefaultInstance().getVersion();
  }
  /**
   * <code>string version = 2;</code>
   * @param value The bytes for version to set.
   */
  private void setVersionBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    version_ = value.toStringUtf8();
    
  }

  public static final int ATTRIBUTES_FIELD_NUMBER = 3;
  private com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> attributes_;
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  @java.lang.Override
  public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> getAttributesList() {
    return attributes_;
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  public java.util.List<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueOrBuilder> 
      getAttributesOrBuilderList() {
    return attributes_;
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  @java.lang.Override
  public int getAttributesCount() {
    return attributes_.size();
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getAttributes(int index) {
    return attributes_.get(index);
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueOrBuilder getAttributesOrBuilder(
      int index) {
    return attributes_.get(index);
  }
  private void ensureAttributesIsMutable() {
    com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> tmp = attributes_;
    if (!tmp.isModifiable()) {
      attributes_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  private void setAttributes(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.set(index, value);
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  private void addAttributes(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.add(value);
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  private void addAttributes(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.add(index, value);
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  private void addAllAttributes(
      java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values) {
    ensureAttributesIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, attributes_);
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  private void clearAttributes() {
    attributes_ = emptyProtobufList();
  }
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  private void removeAttributes(int index) {
    ensureAttributesIsMutable();
    attributes_.remove(index);
  }

  public static final int DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER = 4;
  private int droppedAttributesCount_;
  /**
   * <code>uint32 dropped_attributes_count = 4;</code>
   * @return The droppedAttributesCount.
   */
  @java.lang.Override
  public int getDroppedAttributesCount() {
    return droppedAttributesCount_;
  }
  /**
   * <code>uint32 dropped_attributes_count = 4;</code>
   * @param value The droppedAttributesCount to set.
   */
  private void setDroppedAttributesCount(int value) {
    
    droppedAttributesCount_ = value;
  }
  /**
   * <code>uint32 dropped_attributes_count = 4;</code>
   */
  private void clearDroppedAttributesCount() {
    
    droppedAttributesCount_ = 0;
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * InstrumentationScope is a message representing the instrumentation scope information
   * such as the fully qualified name and version. 
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.common.v1.InstrumentationScope}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.common.v1.InstrumentationScope)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScopeOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * An empty instrumentation scope name means the name is unknown.
     * </pre>
     *
     * <code>string name = 1;</code>
     * @return The name.
     */
    @java.lang.Override
    public java.lang.String getName() {
      return instance.getName();
    }
    /**
     * <pre>
     * An empty instrumentation scope name means the name is unknown.
     * </pre>
     *
     * <code>string name = 1;</code>
     * @return The bytes for name.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNameBytes() {
      return instance.getNameBytes();
    }
    /**
     * <pre>
     * An empty instrumentation scope name means the name is unknown.
     * </pre>
     *
     * <code>string name = 1;</code>
     * @param value The name to set.
     * @return This builder for chaining.
     */
    public Builder setName(
        java.lang.String value) {
      copyOnWrite();
      instance.setName(value);
      return this;
    }
    /**
     * <pre>
     * An empty instrumentation scope name means the name is unknown.
     * </pre>
     *
     * <code>string name = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearName() {
      copyOnWrite();
      instance.clearName();
      return this;
    }
    /**
     * <pre>
     * An empty instrumentation scope name means the name is unknown.
     * </pre>
     *
     * <code>string name = 1;</code>
     * @param value The bytes for name to set.
     * @return This builder for chaining.
     */
    public Builder setNameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setNameBytes(value);
      return this;
    }

    /**
     * <code>string version = 2;</code>
     * @return The version.
     */
    @java.lang.Override
    public java.lang.String getVersion() {
      return instance.getVersion();
    }
    /**
     * <code>string version = 2;</code>
     * @return The bytes for version.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getVersionBytes() {
      return instance.getVersionBytes();
    }
    /**
     * <code>string version = 2;</code>
     * @param value The version to set.
     * @return This builder for chaining.
     */
    public Builder setVersion(
        java.lang.String value) {
      copyOnWrite();
      instance.setVersion(value);
      return this;
    }
    /**
     * <code>string version = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearVersion() {
      copyOnWrite();
      instance.clearVersion();
      return this;
    }
    /**
     * <code>string version = 2;</code>
     * @param value The bytes for version to set.
     * @return This builder for chaining.
     */
    public Builder setVersionBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setVersionBytes(value);
      return this;
    }

    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    @java.lang.Override
    public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> getAttributesList() {
      return java.util.Collections.unmodifiableList(
          instance.getAttributesList());
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    @java.lang.Override
    public int getAttributesCount() {
      return instance.getAttributesCount();
    }/**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getAttributes(int index) {
      return instance.getAttributes(index);
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder setAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.setAttributes(index, value);
      return this;
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder setAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.setAttributes(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder addAttributes(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.addAttributes(value);
      return this;
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder addAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.addAttributes(index, value);
      return this;
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder addAttributes(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addAttributes(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder addAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addAttributes(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder addAllAttributes(
        java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values) {
      copyOnWrite();
      instance.addAllAttributes(values);
      return this;
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder clearAttributes() {
      copyOnWrite();
      instance.clearAttributes();
      return this;
    }
    /**
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
     */
    public Builder removeAttributes(int index) {
      copyOnWrite();
      instance.removeAttributes(index);
      return this;
    }

    /**
     * <code>uint32 dropped_attributes_count = 4;</code>
     * @return The droppedAttributesCount.
     */
    @java.lang.Override
    public int getDroppedAttributesCount() {
      return instance.getDroppedAttributesCount();
    }
    /**
     * <code>uint32 dropped_attributes_count = 4;</code>
     * @param value The droppedAttributesCount to set.
     * @return This builder for chaining.
     */
    public Builder setDroppedAttributesCount(int value) {
      copyOnWrite();
      instance.setDroppedAttributesCount(value);
      return this;
    }
    /**
     * <code>uint32 dropped_attributes_count = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearDroppedAttributesCount() {
      copyOnWrite();
      instance.clearDroppedAttributesCount();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.common.v1.InstrumentationScope)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "name_",
            "version_",
            "attributes_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.class,
            "droppedAttributesCount_",
          };
          java.lang.String info =
              "\u0000\u0004\u0000\u0000\u0001\u0004\u0004\u0000\u0001\u0000\u0001\u0208\u0002\u0208" +
              "\u0003\u001b\u0004\u000b";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.common.v1.InstrumentationScope)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope DEFAULT_INSTANCE;
  static {
    InstrumentationScope defaultInstance = new InstrumentationScope();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      InstrumentationScope.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<InstrumentationScope> PARSER;

  public static com.google.protobuf.Parser<InstrumentationScope> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

