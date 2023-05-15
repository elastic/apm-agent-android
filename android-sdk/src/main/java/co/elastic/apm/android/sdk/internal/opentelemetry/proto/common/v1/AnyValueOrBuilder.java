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

public interface AnyValueOrBuilder extends
    // @@protoc_insertion_point(interface_extends:opentelemetry.proto.common.v1.AnyValue)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>string string_value = 1;</code>
   * @return Whether the stringValue field is set.
   */
  boolean hasStringValue();
  /**
   * <code>string string_value = 1;</code>
   * @return The stringValue.
   */
  java.lang.String getStringValue();
  /**
   * <code>string string_value = 1;</code>
   * @return The bytes for stringValue.
   */
  com.google.protobuf.ByteString
      getStringValueBytes();

  /**
   * <code>bool bool_value = 2;</code>
   * @return Whether the boolValue field is set.
   */
  boolean hasBoolValue();
  /**
   * <code>bool bool_value = 2;</code>
   * @return The boolValue.
   */
  boolean getBoolValue();

  /**
   * <code>int64 int_value = 3;</code>
   * @return Whether the intValue field is set.
   */
  boolean hasIntValue();
  /**
   * <code>int64 int_value = 3;</code>
   * @return The intValue.
   */
  long getIntValue();

  /**
   * <code>double double_value = 4;</code>
   * @return Whether the doubleValue field is set.
   */
  boolean hasDoubleValue();
  /**
   * <code>double double_value = 4;</code>
   * @return The doubleValue.
   */
  double getDoubleValue();

  /**
   * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
   * @return Whether the arrayValue field is set.
   */
  boolean hasArrayValue();
  /**
   * <code>.opentelemetry.proto.common.v1.ArrayValue array_value = 5;</code>
   * @return The arrayValue.
   */
  co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.ArrayValue getArrayValue();

  /**
   * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
   * @return Whether the kvlistValue field is set.
   */
  boolean hasKvlistValue();
  /**
   * <code>.opentelemetry.proto.common.v1.KeyValueList kvlist_value = 6;</code>
   * @return The kvlistValue.
   */
  co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueList getKvlistValue();

  /**
   * <code>bytes bytes_value = 7;</code>
   * @return Whether the bytesValue field is set.
   */
  boolean hasBytesValue();
  /**
   * <code>bytes bytes_value = 7;</code>
   * @return The bytesValue.
   */
  com.google.protobuf.ByteString getBytesValue();

  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.ValueCase getValueCase();
}
