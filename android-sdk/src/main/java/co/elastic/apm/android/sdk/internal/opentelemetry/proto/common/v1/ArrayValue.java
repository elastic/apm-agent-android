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
 * ArrayValue is a list of AnyValue messages. We need ArrayValue as a message
 * since oneof in AnyValue does not allow repeated fields.
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.common.v1.ArrayValue}
 */
public  final class ArrayValue extends
    com.google.protobuf.GeneratedMessageLite<
        ArrayValue, ArrayValue.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.common.v1.ArrayValue)
    ArrayValueOrBuilder {
  private ArrayValue() {
    values_ = emptyProtobufList();
  }
  public static final int VALUES_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue> values_;
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  @java.lang.Override
  public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue> getValuesList() {
    return values_;
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  public java.util.List<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValueOrBuilder> 
      getValuesOrBuilderList() {
    return values_;
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  @java.lang.Override
  public int getValuesCount() {
    return values_.size();
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue getValues(int index) {
    return values_.get(index);
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValueOrBuilder getValuesOrBuilder(
      int index) {
    return values_.get(index);
  }
  private void ensureValuesIsMutable() {
    com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue> tmp = values_;
    if (!tmp.isModifiable()) {
      values_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  private void setValues(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
    value.getClass();
  ensureValuesIsMutable();
    values_.set(index, value);
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  private void addValues(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
    value.getClass();
  ensureValuesIsMutable();
    values_.add(value);
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  private void addValues(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
    value.getClass();
  ensureValuesIsMutable();
    values_.add(index, value);
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  private void addAllValues(
      java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue> values) {
    ensureValuesIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, values_);
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  private void clearValues() {
    values_ = emptyProtobufList();
  }
  /**
   * <pre>
   * Array of values. The array may be empty (contain 0 elements).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
   */
  private void removeValues(int index) {
    ensureValuesIsMutable();
    values_.remove(index);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * ArrayValue is a list of AnyValue messages. We need ArrayValue as a message
   * since oneof in AnyValue does not allow repeated fields.
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.common.v1.ArrayValue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.common.v1.ArrayValue)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValueOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    @java.lang.Override
    public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue> getValuesList() {
      return java.util.Collections.unmodifiableList(
          instance.getValuesList());
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    @java.lang.Override
    public int getValuesCount() {
      return instance.getValuesCount();
    }/**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue getValues(int index) {
      return instance.getValues(index);
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder setValues(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
      copyOnWrite();
      instance.setValues(index, value);
      return this;
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder setValues(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.Builder builderForValue) {
      copyOnWrite();
      instance.setValues(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder addValues(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
      copyOnWrite();
      instance.addValues(value);
      return this;
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder addValues(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
      copyOnWrite();
      instance.addValues(index, value);
      return this;
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder addValues(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addValues(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder addValues(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addValues(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder addAllValues(
        java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue> values) {
      copyOnWrite();
      instance.addAllValues(values);
      return this;
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder clearValues() {
      copyOnWrite();
      instance.clearValues();
      return this;
    }
    /**
     * <pre>
     * Array of values. The array may be empty (contain 0 elements).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.AnyValue values = 1;</code>
     */
    public Builder removeValues(int index) {
      copyOnWrite();
      instance.removeValues(index);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.common.v1.ArrayValue)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "values_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.class,
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
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.common.v1.ArrayValue)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue DEFAULT_INSTANCE;
  static {
    ArrayValue defaultInstance = new ArrayValue();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ArrayValue.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ArrayValue> PARSER;

  public static com.google.protobuf.Parser<ArrayValue> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

