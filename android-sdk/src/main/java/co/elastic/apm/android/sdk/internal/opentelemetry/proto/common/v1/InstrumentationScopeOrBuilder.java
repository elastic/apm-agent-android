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

public interface InstrumentationScopeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:opentelemetry.proto.common.v1.InstrumentationScope)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <pre>
   * An empty instrumentation scope name means the name is unknown.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * An empty instrumentation scope name means the name is unknown.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string version = 2;</code>
   * @return The version.
   */
  java.lang.String getVersion();
  /**
   * <code>string version = 2;</code>
   * @return The bytes for version.
   */
  com.google.protobuf.ByteString
      getVersionBytes();

  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> 
      getAttributesList();
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getAttributes(int index);
  /**
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 3;</code>
   */
  int getAttributesCount();

  /**
   * <code>uint32 dropped_attributes_count = 4;</code>
   * @return The droppedAttributesCount.
   */
  int getDroppedAttributesCount();
}
