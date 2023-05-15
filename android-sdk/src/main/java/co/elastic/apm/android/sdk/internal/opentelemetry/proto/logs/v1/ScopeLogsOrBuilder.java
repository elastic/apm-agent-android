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

public interface ScopeLogsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:opentelemetry.proto.logs.v1.ScopeLogs)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <pre>
   * The instrumentation scope information for the logs in this message.
   * Semantically when InstrumentationScope isn't set, it is equivalent with
   * an empty instrumentation scope name (unknown).
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
   * @return Whether the scope field is set.
   */
  boolean hasScope();
  /**
   * <pre>
   * The instrumentation scope information for the logs in this message.
   * Semantically when InstrumentationScope isn't set, it is equivalent with
   * an empty instrumentation scope name (unknown).
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.InstrumentationScope scope = 1;</code>
   * @return The scope.
   */
  co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope getScope();

  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord> 
      getLogRecordsList();
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord getLogRecords(int index);
  /**
   * <pre>
   * A list of log records.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.LogRecord log_records = 2;</code>
   */
  int getLogRecordsCount();

  /**
   * <pre>
   * This schema_url applies to all logs in the "logs" field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   * @return The schemaUrl.
   */
  java.lang.String getSchemaUrl();
  /**
   * <pre>
   * This schema_url applies to all logs in the "logs" field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   * @return The bytes for schemaUrl.
   */
  com.google.protobuf.ByteString
      getSchemaUrlBytes();
}
