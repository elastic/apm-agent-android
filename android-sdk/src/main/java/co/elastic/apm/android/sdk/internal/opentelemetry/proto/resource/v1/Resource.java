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
package co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1;

/**
 * <pre>
 * Resource information.
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.resource.v1.Resource}
 */
public  final class Resource extends
    com.google.protobuf.GeneratedMessageLite<
        Resource, Resource.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.resource.v1.Resource)
    ResourceOrBuilder {
  private Resource() {
    attributes_ = emptyProtobufList();
  }
  public static final int ATTRIBUTES_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> attributes_;
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  @java.lang.Override
  public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> getAttributesList() {
    return attributes_;
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  public java.util.List<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueOrBuilder> 
      getAttributesOrBuilderList() {
    return attributes_;
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  @java.lang.Override
  public int getAttributesCount() {
    return attributes_.size();
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getAttributes(int index) {
    return attributes_.get(index);
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
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
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  private void setAttributes(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.set(index, value);
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  private void addAttributes(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.add(value);
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  private void addAttributes(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.add(index, value);
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  private void addAllAttributes(
      java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values) {
    ensureAttributesIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, attributes_);
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  private void clearAttributes() {
    attributes_ = emptyProtobufList();
  }
  /**
   * <pre>
   * Set of attributes that describe the resource.
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
   */
  private void removeAttributes(int index) {
    ensureAttributesIsMutable();
    attributes_.remove(index);
  }

  public static final int DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER = 2;
  private int droppedAttributesCount_;
  /**
   * <pre>
   * dropped_attributes_count is the number of dropped attributes. If the value is 0, then
   * no attributes were dropped.
   * </pre>
   *
   * <code>uint32 dropped_attributes_count = 2;</code>
   * @return The droppedAttributesCount.
   */
  @java.lang.Override
  public int getDroppedAttributesCount() {
    return droppedAttributesCount_;
  }
  /**
   * <pre>
   * dropped_attributes_count is the number of dropped attributes. If the value is 0, then
   * no attributes were dropped.
   * </pre>
   *
   * <code>uint32 dropped_attributes_count = 2;</code>
   * @param value The droppedAttributesCount to set.
   */
  private void setDroppedAttributesCount(int value) {
    
    droppedAttributesCount_ = value;
  }
  /**
   * <pre>
   * dropped_attributes_count is the number of dropped attributes. If the value is 0, then
   * no attributes were dropped.
   * </pre>
   *
   * <code>uint32 dropped_attributes_count = 2;</code>
   */
  private void clearDroppedAttributesCount() {
    
    droppedAttributesCount_ = 0;
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * Resource information.
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.resource.v1.Resource}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.resource.v1.Resource)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.ResourceOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    @java.lang.Override
    public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> getAttributesList() {
      return java.util.Collections.unmodifiableList(
          instance.getAttributesList());
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    @java.lang.Override
    public int getAttributesCount() {
      return instance.getAttributesCount();
    }/**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getAttributes(int index) {
      return instance.getAttributes(index);
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder setAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.setAttributes(index, value);
      return this;
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder setAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.setAttributes(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder addAttributes(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.addAttributes(value);
      return this;
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder addAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.addAttributes(index, value);
      return this;
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder addAttributes(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addAttributes(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder addAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addAttributes(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder addAllAttributes(
        java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values) {
      copyOnWrite();
      instance.addAllAttributes(values);
      return this;
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder clearAttributes() {
      copyOnWrite();
      instance.clearAttributes();
      return this;
    }
    /**
     * <pre>
     * Set of attributes that describe the resource.
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 1;</code>
     */
    public Builder removeAttributes(int index) {
      copyOnWrite();
      instance.removeAttributes(index);
      return this;
    }

    /**
     * <pre>
     * dropped_attributes_count is the number of dropped attributes. If the value is 0, then
     * no attributes were dropped.
     * </pre>
     *
     * <code>uint32 dropped_attributes_count = 2;</code>
     * @return The droppedAttributesCount.
     */
    @java.lang.Override
    public int getDroppedAttributesCount() {
      return instance.getDroppedAttributesCount();
    }
    /**
     * <pre>
     * dropped_attributes_count is the number of dropped attributes. If the value is 0, then
     * no attributes were dropped.
     * </pre>
     *
     * <code>uint32 dropped_attributes_count = 2;</code>
     * @param value The droppedAttributesCount to set.
     * @return This builder for chaining.
     */
    public Builder setDroppedAttributesCount(int value) {
      copyOnWrite();
      instance.setDroppedAttributesCount(value);
      return this;
    }
    /**
     * <pre>
     * dropped_attributes_count is the number of dropped attributes. If the value is 0, then
     * no attributes were dropped.
     * </pre>
     *
     * <code>uint32 dropped_attributes_count = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearDroppedAttributesCount() {
      copyOnWrite();
      instance.clearDroppedAttributesCount();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.resource.v1.Resource)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "attributes_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.class,
            "droppedAttributesCount_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0001\u0000\u0001\u001b\u0002\u000b" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.resource.v1.Resource)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource DEFAULT_INSTANCE;
  static {
    Resource defaultInstance = new Resource();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Resource.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Resource> PARSER;

  public static com.google.protobuf.Parser<Resource> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
