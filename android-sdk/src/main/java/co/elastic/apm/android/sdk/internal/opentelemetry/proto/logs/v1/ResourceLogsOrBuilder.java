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

public interface ResourceLogsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:opentelemetry.proto.logs.v1.ResourceLogs)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <pre>
   * The resource for the logs in this message.
   * If this field is not set then resource info is unknown.
   * </pre>
   *
   * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
   * @return Whether the resource field is set.
   */
  boolean hasResource();
  /**
   * <pre>
   * The resource for the logs in this message.
   * If this field is not set then resource info is unknown.
   * </pre>
   *
   * <code>.opentelemetry.proto.resource.v1.Resource resource = 1;</code>
   * @return The resource.
   */
  co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource getResource();

  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs> 
      getScopeLogsList();
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs getScopeLogs(int index);
  /**
   * <pre>
   * A list of ScopeLogs that originate from a resource.
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.logs.v1.ScopeLogs scope_logs = 2;</code>
   */
  int getScopeLogsCount();

  /**
   * <pre>
   * This schema_url applies to the data in the "resource" field. It does not apply
   * to the data in the "scope_logs" field which have their own schema_url field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   * @return The schemaUrl.
   */
  java.lang.String getSchemaUrl();
  /**
   * <pre>
   * This schema_url applies to the data in the "resource" field. It does not apply
   * to the data in the "scope_logs" field which have their own schema_url field.
   * </pre>
   *
   * <code>string schema_url = 3;</code>
   * @return The bytes for schemaUrl.
   */
  com.google.protobuf.ByteString
      getSchemaUrlBytes();
}
