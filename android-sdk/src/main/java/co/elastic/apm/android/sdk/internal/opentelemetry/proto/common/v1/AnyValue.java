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
 * AnyValue is used to represent any type of attribute value. AnyValue may contain a
 * primitive value such as a string or integer or it may contain an arbitrary nested
 * object containing arrays, key-value lists and primitives.
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.common.v1.AnyValue}
 */
public  final class AnyValue extends
    com.google.protobuf.GeneratedMessageLite<
        AnyValue, AnyValue.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.common.v1.AnyValue)
    AnyValueOrBuilder {
  private AnyValue() {
  }
  private int valueCase_ = 0;
  private java.lang.Object value_;
  public enum ValueCase {
    STRING_VALUE(1),
    BOOL_VALUE(2),
    INT_VALUE(3),
    DOUBLE_VALUE(4),
    ARRAY_VALUE(5),
    KVLIST_VALUE(6),
    BYTES_VALUE(7),
    VALUE_NOT_SET(0);
    private final int value;
    private ValueCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static ValueCase valueOf(int value) {
      return forNumber(value);
    }

    public static ValueCase forNumber(int value) {
      switch (value) {
        case 1: return STRING_VALUE;
        case 2: return BOOL_VALUE;
        case 3: return INT_VALUE;
        case 4: return DOUBLE_VALUE;
        case 5: return ARRAY_VALUE;
        case 6: return KVLIST_VALUE;
        case 7: return BYTES_VALUE;
        case 0: return VALUE_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  @java.lang.Override
  public ValueCase
  getValueCase() {
    return ValueCase.forNumber(
        valueCase_);
  }

  private void clearValue() {
    valueCase_ = 0;
    value_ = null;
  }

  public static final int STRING_VALUE_FIELD_NUMBER = 1;
  /**
   * <code>string string_value = 1;</code>
   * @return Whether the stringValue field is set.
   */
  @java.lang.Override
  public boolean hasStringValue() {
    return valueCase_ == 1;
  }
  /**
   * <code>string string_value = 1;</code>
   * @return The stringValue.
   */
  @java.lang.Override
  public java.lang.String getStringValue() {
    java.lang.String ref = "";
    if (valueCase_ == 1) {
      ref = (java.lang.String) value_;
    }
    return ref;
  }
  /**
   * <code>string string_value = 1;</code>
   * @return The bytes for stringValue.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getStringValueBytes() {
    java.lang.String ref = "";
    if (valueCase_ == 1) {
      ref = (java.lang.String) value_;
    }
    return com.google.protobuf.ByteString.copyFromUtf8(ref);
  }
  /**
   * <code>string string_value = 1;</code>
   * @param value The stringValue to set.
   */
  private void setStringValue(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  valueCase_ = 1;
    value_ = value;
  }
  /**
   * <code>string string_value = 1;</code>
   */
  private void clearStringValue() {
    if (valueCase_ == 1) {
      valueCase_ = 0;
      value_ = null;
    }
  }
  /**
   * <code>string string_value = 1;</code>
   * @param value The bytes for stringValue to set.
   */
  private void setStringValueBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    value_ = value.toStringUtf8();
    valueCase_ = 1;
  }

  public static final int BOOL_VALUE_FIELD_NUMBER = 2;
  /**
   * <code>bool bool_value = 2;</code>
   * @return Whether the boolValue field is set.
   */
  @java.lang.Override
  public boolean hasBoolValue() {
    return valueCase_ == 2;
  }
  /**
   * <code>bool bool_value = 2;</code>
   * @return The boolValue.
   */
  @java.lang.Override
  public boolean getBoolValue() {
    if (valueCase_ == 2) {
      return (java.lang.Boolean) value_;
    }
    return false;
  }
  /**
   * <code>bool bool_value = 2;</code>
   * @param value The boolValue to set.
   */
  private void setBoolValue(boolean value) {
    valueCase_ = 2;
    value_ = value;
  }
  /**
   * <code>bool bool_value = 2;</code>
   */
  private void clearBoolValue() {
    if (valueCase_ == 2) {
      valueCase_ = 0;
      value_ = null;
    }
  }

  public static final int INT_VALUE_FIELD_NUMBER = 3;
  /**
   * <code>int64 int_value = 3;</code>
   * @return Whether the intValue field is set.
   */
  @java.lang.Override
  public boolean hasIntValue() {
    return valueCase_ == 3;
  }
  /**
   * <code>int64 int_value = 3;</code>
   * @return The intValue.
   */
  @java.lang.Override
  public long getIntValue() {
    if (valueCase_ == 3) {
      return (java.lang.Long) value_;
    }
    return 0L;
  }
  /**
   * <code>int64 int_value = 3;</code>
   * @param value The intValue to set.
   */
  private void setIntValue(long value) {
    valueCase_ = 3;
    value_ = value;
  }
  /**
   * <code>int64 int_value = 3;</code>
   */
  private void clearIntValue() {
    if (valueCase_ == 3) {
      valueCase_ = 0;
      value_ = null;
    }
  }

  public static final int DOUBLE_VALUE_FIELD_NUMBER = 4;
  /**
   * <code>double double_value = 4;</code>
   * @return Whether the doubleValue field is set.
   */
  @java.lang.Override
  public boolean hasDoubleValue() {
    return valueCase_ == 4;
  }
  /**
   * <code>double double_value = 4;</code>
   * @return The doubleValue.
   */
  @java.lang.Override
  public double getDoubleValue() {
    if (valueCase_ == 4) {
      return (java.lang.Double) value_;
    }
    return 0D;
  }
  /**
   * <code>double double_value = 4;</code>
   * @param value The doubleValue to set.
   */
  private void setDoubleValue(double value) {
    valueCase_ = 4;
    value_ = value;
  }
  /**
   * <code>double double_value = 4;</code>
   */
  private void clearDoubleValue() {
    if (valueCase_ == 4) {
      valueCase_ = 0;
      value_ = null;
    }
  }

  public static final int ARRAY_VALUE_FIELD_NUMBER = 5;
  /**
   * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
   */
  @java.lang.Override
  public boolean hasArrayValue() {
    return valueCase_ == 5;
  }
  /**
   * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue getArrayValue() {
    if (valueCase_ == 5) {
       return (co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue) value_;
    }
    return co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue.getDefaultInstance();
  }
  /**
   * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
   */
  private void setArrayValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue value) {
    value.getClass();
  value_ = value;
    valueCase_ = 5;
  }
  /**
   * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
   */
  private void mergeArrayValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue value) {
    value.getClass();
  if (valueCase_ == 5 &&
        value_ != co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue.getDefaultInstance()) {
      value_ = co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue.newBuilder((co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue) value_)
          .mergeFrom(value).buildPartial();
    } else {
      value_ = value;
    }
    valueCase_ = 5;
  }
  /**
   * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
   */
  private void clearArrayValue() {
    if (valueCase_ == 5) {
      valueCase_ = 0;
      value_ = null;
    }
  }

  public static final int KVLIST_VALUE_FIELD_NUMBER = 6;
  /**
   * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
   */
  @java.lang.Override
  public boolean hasKvlistValue() {
    return valueCase_ == 6;
  }
  /**
   * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList getKvlistValue() {
    if (valueCase_ == 6) {
       return (co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList) value_;
    }
    return co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList.getDefaultInstance();
  }
  /**
   * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
   */
  private void setKvlistValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList value) {
    value.getClass();
  value_ = value;
    valueCase_ = 6;
  }
  /**
   * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
   */
  private void mergeKvlistValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList value) {
    value.getClass();
  if (valueCase_ == 6 &&
        value_ != co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList.getDefaultInstance()) {
      value_ = co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList.newBuilder((co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList) value_)
          .mergeFrom(value).buildPartial();
    } else {
      value_ = value;
    }
    valueCase_ = 6;
  }
  /**
   * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
   */
  private void clearKvlistValue() {
    if (valueCase_ == 6) {
      valueCase_ = 0;
      value_ = null;
    }
  }

  public static final int BYTES_VALUE_FIELD_NUMBER = 7;
  /**
   * <code>bytes bytes_value = 7;</code>
   * @return Whether the bytesValue field is set.
   */
  @java.lang.Override
  public boolean hasBytesValue() {
    return valueCase_ == 7;
  }
  /**
   * <code>bytes bytes_value = 7;</code>
   * @return The bytesValue.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getBytesValue() {
    if (valueCase_ == 7) {
      return (com.google.protobuf.ByteString) value_;
    }
    return com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * <code>bytes bytes_value = 7;</code>
   * @param value The bytesValue to set.
   */
  private void setBytesValue(com.google.protobuf.ByteString value) {
    java.lang.Class<?> valueClass = value.getClass();
  valueCase_ = 7;
    value_ = value;
  }
  /**
   * <code>bytes bytes_value = 7;</code>
   */
  private void clearBytesValue() {
    if (valueCase_ == 7) {
      valueCase_ = 0;
      value_ = null;
    }
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * AnyValue is used to represent any type of attribute value. AnyValue may contain a
   * primitive value such as a string or integer or it may contain an arbitrary nested
   * object containing arrays, key-value lists and primitives.
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.common.v1.AnyValue}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.common.v1.AnyValue)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValueOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }

    @java.lang.Override
    public ValueCase
        getValueCase() {
      return instance.getValueCase();
    }

    public Builder clearValue() {
      copyOnWrite();
      instance.clearValue();
      return this;
    }


    /**
     * <code>string string_value = 1;</code>
     * @return Whether the stringValue field is set.
     */
    @java.lang.Override
    public boolean hasStringValue() {
      return instance.hasStringValue();
    }
    /**
     * <code>string string_value = 1;</code>
     * @return The stringValue.
     */
    @java.lang.Override
    public java.lang.String getStringValue() {
      return instance.getStringValue();
    }
    /**
     * <code>string string_value = 1;</code>
     * @return The bytes for stringValue.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getStringValueBytes() {
      return instance.getStringValueBytes();
    }
    /**
     * <code>string string_value = 1;</code>
     * @param value The stringValue to set.
     * @return This builder for chaining.
     */
    public Builder setStringValue(
        java.lang.String value) {
      copyOnWrite();
      instance.setStringValue(value);
      return this;
    }
    /**
     * <code>string string_value = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearStringValue() {
      copyOnWrite();
      instance.clearStringValue();
      return this;
    }
    /**
     * <code>string string_value = 1;</code>
     * @param value The bytes for stringValue to set.
     * @return This builder for chaining.
     */
    public Builder setStringValueBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setStringValueBytes(value);
      return this;
    }

    /**
     * <code>bool bool_value = 2;</code>
     * @return Whether the boolValue field is set.
     */
    @java.lang.Override
    public boolean hasBoolValue() {
      return instance.hasBoolValue();
    }
    /**
     * <code>bool bool_value = 2;</code>
     * @return The boolValue.
     */
    @java.lang.Override
    public boolean getBoolValue() {
      return instance.getBoolValue();
    }
    /**
     * <code>bool bool_value = 2;</code>
     * @param value The boolValue to set.
     * @return This builder for chaining.
     */
    public Builder setBoolValue(boolean value) {
      copyOnWrite();
      instance.setBoolValue(value);
      return this;
    }
    /**
     * <code>bool bool_value = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearBoolValue() {
      copyOnWrite();
      instance.clearBoolValue();
      return this;
    }

    /**
     * <code>int64 int_value = 3;</code>
     * @return Whether the intValue field is set.
     */
    @java.lang.Override
    public boolean hasIntValue() {
      return instance.hasIntValue();
    }
    /**
     * <code>int64 int_value = 3;</code>
     * @return The intValue.
     */
    @java.lang.Override
    public long getIntValue() {
      return instance.getIntValue();
    }
    /**
     * <code>int64 int_value = 3;</code>
     * @param value The intValue to set.
     * @return This builder for chaining.
     */
    public Builder setIntValue(long value) {
      copyOnWrite();
      instance.setIntValue(value);
      return this;
    }
    /**
     * <code>int64 int_value = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearIntValue() {
      copyOnWrite();
      instance.clearIntValue();
      return this;
    }

    /**
     * <code>double double_value = 4;</code>
     * @return Whether the doubleValue field is set.
     */
    @java.lang.Override
    public boolean hasDoubleValue() {
      return instance.hasDoubleValue();
    }
    /**
     * <code>double double_value = 4;</code>
     * @return The doubleValue.
     */
    @java.lang.Override
    public double getDoubleValue() {
      return instance.getDoubleValue();
    }
    /**
     * <code>double double_value = 4;</code>
     * @param value The doubleValue to set.
     * @return This builder for chaining.
     */
    public Builder setDoubleValue(double value) {
      copyOnWrite();
      instance.setDoubleValue(value);
      return this;
    }
    /**
     * <code>double double_value = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearDoubleValue() {
      copyOnWrite();
      instance.clearDoubleValue();
      return this;
    }

    /**
     * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
     */
    @java.lang.Override
    public boolean hasArrayValue() {
      return instance.hasArrayValue();
    }
    /**
     * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue getArrayValue() {
      return instance.getArrayValue();
    }
    /**
     * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
     */
    public Builder setArrayValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue value) {
      copyOnWrite();
      instance.setArrayValue(value);
      return this;
    }
    /**
     * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
     */
    public Builder setArrayValue(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue.Builder builderForValue) {
      copyOnWrite();
      instance.setArrayValue(builderForValue.build());
      return this;
    }
    /**
     * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
     */
    public Builder mergeArrayValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue value) {
      copyOnWrite();
      instance.mergeArrayValue(value);
      return this;
    }
    /**
     * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
     */
    public Builder clearArrayValue() {
      copyOnWrite();
      instance.clearArrayValue();
      return this;
    }

    /**
     * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
     */
    @java.lang.Override
    public boolean hasKvlistValue() {
      return instance.hasKvlistValue();
    }
    /**
     * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList getKvlistValue() {
      return instance.getKvlistValue();
    }
    /**
     * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
     */
    public Builder setKvlistValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList value) {
      copyOnWrite();
      instance.setKvlistValue(value);
      return this;
    }
    /**
     * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
     */
    public Builder setKvlistValue(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList.Builder builderForValue) {
      copyOnWrite();
      instance.setKvlistValue(builderForValue.build());
      return this;
    }
    /**
     * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
     */
    public Builder mergeKvlistValue(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList value) {
      copyOnWrite();
      instance.mergeKvlistValue(value);
      return this;
    }
    /**
     * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
     */
    public Builder clearKvlistValue() {
      copyOnWrite();
      instance.clearKvlistValue();
      return this;
    }

    /**
     * <code>bytes bytes_value = 7;</code>
     * @return Whether the bytesValue field is set.
     */
    @java.lang.Override
    public boolean hasBytesValue() {
      return instance.hasBytesValue();
    }
    /**
     * <code>bytes bytes_value = 7;</code>
     * @return The bytesValue.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBytesValue() {
      return instance.getBytesValue();
    }
    /**
     * <code>bytes bytes_value = 7;</code>
     * @param value The bytesValue to set.
     * @return This builder for chaining.
     */
    public Builder setBytesValue(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setBytesValue(value);
      return this;
    }
    /**
     * <code>bytes bytes_value = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearBytesValue() {
      copyOnWrite();
      instance.clearBytesValue();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.common.v1.AnyValue)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "value_",
            "valueCase_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue.class,
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList.class,
          };
          java.lang.String info =
              "\u0000\u0007\u0001\u0000\u0001\u0007\u0007\u0000\u0000\u0000\u0001\u023b\u0000\u0002" +
              ":\u0000\u00035\u0000\u00043\u0000\u0005<\u0000\u0006<\u0000\u0007=\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.common.v1.AnyValue)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue DEFAULT_INSTANCE;
  static {
    AnyValue defaultInstance = new AnyValue();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      AnyValue.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<AnyValue> PARSER;

  public static com.google.protobuf.Parser<AnyValue> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
