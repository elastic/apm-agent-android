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
 * KeyValueList is a list of KeyValue messages. We need KeyValueList as a message
 * since `oneof` in AnyValue does not allow repeated fields. Everywhere else where we need
 * a list of KeyValue messages (e.g. in Span) we use `repeated KeyValue` directly to
 * avoid unnecessary extra wrapping (which slows down the protocol). The 2 approaches
 * are semantically equivalent.
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.common.v1.KeyValueList}
 */
public  final class KeyValueList extends
    com.google.protobuf.GeneratedMessageLite<
        KeyValueList, KeyValueList.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.common.v1.KeyValueList)
    KeyValueListOrBuilder {
  private KeyValueList() {
    values_ = emptyProtobufList();
  }
  public static final int VALUES_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values_;
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  @java.lang.Override
  public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> getValuesList() {
    return values_;
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  public java.util.List<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueOrBuilder> 
      getValuesOrBuilderList() {
    return values_;
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  @java.lang.Override
  public int getValuesCount() {
    return values_.size();
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getValues(int index) {
    return values_.get(index);
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueOrBuilder getValuesOrBuilder(
      int index) {
    return values_.get(index);
  }
  private void ensureValuesIsMutable() {
    com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> tmp = values_;
    if (!tmp.isModifiable()) {
      values_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  private void setValues(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureValuesIsMutable();
    values_.set(index, value);
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  private void addValues(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureValuesIsMutable();
    values_.add(value);
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  private void addValues(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureValuesIsMutable();
    values_.add(index, value);
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  private void addAllValues(
      java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values) {
    ensureValuesIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, values_);
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  private void clearValues() {
    values_ = emptyProtobufList();
  }
  /**
   * <pre>
   * A collection of key/value pairs of key-value pairs. The list may be empty (may
   * contain 0 elements).
   * The keys MUST be unique (it is not allowed to have more than one
   * value with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
   */
  private void removeValues(int index) {
    ensureValuesIsMutable();
    values_.remove(index);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * KeyValueList is a list of KeyValue messages. We need KeyValueList as a message
   * since `oneof` in AnyValue does not allow repeated fields. Everywhere else where we need
   * a list of KeyValue messages (e.g. in Span) we use `repeated KeyValue` directly to
   * avoid unnecessary extra wrapping (which slows down the protocol). The 2 approaches
   * are semantically equivalent.
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.common.v1.KeyValueList}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.common.v1.KeyValueList)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueListOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    @java.lang.Override
    public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> getValuesList() {
      return java.util.Collections.unmodifiableList(
          instance.getValuesList());
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    @java.lang.Override
    public int getValuesCount() {
      return instance.getValuesCount();
    }/**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getValues(int index) {
      return instance.getValues(index);
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder setValues(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.setValues(index, value);
      return this;
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder setValues(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.setValues(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder addValues(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.addValues(value);
      return this;
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder addValues(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.addValues(index, value);
      return this;
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder addValues(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addValues(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder addValues(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addValues(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder addAllValues(
        java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values) {
      copyOnWrite();
      instance.addAllValues(values);
      return this;
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder clearValues() {
      copyOnWrite();
      instance.clearValues();
      return this;
    }
    /**
     * <pre>
     * A collection of key/value pairs of key-value pairs. The list may be empty (may
     * contain 0 elements).
     * The keys MUST be unique (it is not allowed to have more than one
     * value with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue values = 1;</code>
     */
    public Builder removeValues(int index) {
      copyOnWrite();
      instance.removeValues(index);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.common.v1.KeyValueList)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "values_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.class,
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
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.common.v1.KeyValueList)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList DEFAULT_INSTANCE;
  static {
    KeyValueList defaultInstance = new KeyValueList();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      KeyValueList.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<KeyValueList> PARSER;

  public static com.google.protobuf.Parser<KeyValueList> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
