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
 * KeyValue is a key-value pair that is used to store Span attributes, Link
 * attributes, etc.
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.common.v1.KeyValue}
 */
public  final class KeyValue extends
    com.google.protobuf.GeneratedMessageLite<
        KeyValue, KeyValue.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.common.v1.KeyValue)
    KeyValueOrBuilder {
  private KeyValue() {
    key_ = "";
  }
  public static final int KEY_FIELD_NUMBER = 1;
  private java.lang.String key_;
  /**
   * <code>string key = 1;</code>
   * @return The key.
   */
  @java.lang.Override
  public java.lang.String getKey() {
    return key_;
  }
  /**
   * <code>string key = 1;</code>
   * @return The bytes for key.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getKeyBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(key_);
  }
  /**
   * <code>string key = 1;</code>
   * @param value The key to set.
   */
  private void setKey(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    key_ = value;
  }
  /**
   * <code>string key = 1;</code>
   */
  private void clearKey() {
    
    key_ = getDefaultInstance().getKey();
  }
  /**
   * <code>string key = 1;</code>
   * @param value The bytes for key to set.
   */
  private void setKeyBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    key_ = value.toStringUtf8();
    
  }

  public static final int VALUE_FIELD_NUMBER = 2;
  private co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value_;
  /**
   * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
   */
  @java.lang.Override
  public boolean hasValue() {
    return value_ != null;
  }
  /**
   * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue getValue() {
    return value_ == null ? co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.getDefaultInstance() : value_;
  }
  /**
   * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
   */
  private void setValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
    value.getClass();
  value_ = value;
    
    }
  /**
   * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
    value.getClass();
  if (value_ != null &&
        value_ != co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.getDefaultInstance()) {
      value_ =
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.newBuilder(value_).mergeFrom(value).buildPartial();
    } else {
      value_ = value;
    }
    
  }
  /**
   * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
   */
  private void clearValue() {  value_ = null;
    
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * KeyValue is a key-value pair that is used to store Span attributes, Link
   * attributes, etc.
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.common.v1.KeyValue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.common.v1.KeyValue)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>string key = 1;</code>
     * @return The key.
     */
    @java.lang.Override
    public java.lang.String getKey() {
      return instance.getKey();
    }
    /**
     * <code>string key = 1;</code>
     * @return The bytes for key.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getKeyBytes() {
      return instance.getKeyBytes();
    }
    /**
     * <code>string key = 1;</code>
     * @param value The key to set.
     * @return This builder for chaining.
     */
    public Builder setKey(
        java.lang.String value) {
      copyOnWrite();
      instance.setKey(value);
      return this;
    }
    /**
     * <code>string key = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearKey() {
      copyOnWrite();
      instance.clearKey();
      return this;
    }
    /**
     * <code>string key = 1;</code>
     * @param value The bytes for key to set.
     * @return This builder for chaining.
     */
    public Builder setKeyBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setKeyBytes(value);
      return this;
    }

    /**
     * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
     */
    @java.lang.Override
    public boolean hasValue() {
      return instance.hasValue();
    }
    /**
     * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue getValue() {
      return instance.getValue();
    }
    /**
     * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
     */
    public Builder setValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
      copyOnWrite();
      instance.setValue(value);
      return this;
      }
    /**
     * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
     */
    public Builder setValue(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.Builder builderForValue) {
      copyOnWrite();
      instance.setValue(builderForValue.build());
      return this;
    }
    /**
     * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
     */
    public Builder mergeValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
      copyOnWrite();
      instance.mergeValue(value);
      return this;
    }
    /**
     * <code>.opentelemetry.proto.common.v1.AnyValue value = 2;</code>
     */
    public Builder clearValue() {  copyOnWrite();
      instance.clearValue();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.common.v1.KeyValue)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "key_",
            "value_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u0208\u0002\t" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.common.v1.KeyValue)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue DEFAULT_INSTANCE;
  static {
    KeyValue defaultInstance = new KeyValue();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      KeyValue.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<KeyValue> PARSER;

  public static com.google.protobuf.Parser<KeyValue> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
