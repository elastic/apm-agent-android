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

/**
 * <pre>
 * Masks for LogRecord.flags field.
 * </pre>
 *
 * Protobuf enum {@code opentelemetry.proto.logs.v1.LogRecordFlags}
 */
public enum LogRecordFlags
    implements com.google.protobuf.Internal.EnumLite {
  /**
   * <code>LOG_RECORD_FLAG_UNSPECIFIED = 0;</code>
   */
  LOG_RECORD_FLAG_UNSPECIFIED(0),
  /**
   * <code>LOG_RECORD_FLAG_TRACE_FLAGS_MASK = 255;</code>
   */
  LOG_RECORD_FLAG_TRACE_FLAGS_MASK(255),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>LOG_RECORD_FLAG_UNSPECIFIED = 0;</code>
   */
  public static final int LOG_RECORD_FLAG_UNSPECIFIED_VALUE = 0;
  /**
   * <code>LOG_RECORD_FLAG_TRACE_FLAGS_MASK = 255;</code>
   */
  public static final int LOG_RECORD_FLAG_TRACE_FLAGS_MASK_VALUE = 255;


  @java.lang.Override
  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The number of the enum to look for.
   * @return The enum associated with the given number.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static LogRecordFlags valueOf(int value) {
    return forNumber(value);
  }

  public static LogRecordFlags forNumber(int value) {
    switch (value) {
      case 0: return LOG_RECORD_FLAG_UNSPECIFIED;
      case 255: return LOG_RECORD_FLAG_TRACE_FLAGS_MASK;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<LogRecordFlags>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      LogRecordFlags> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<LogRecordFlags>() {
          @java.lang.Override
          public LogRecordFlags findValueByNumber(int number) {
            return LogRecordFlags.forNumber(number);
          }
        };

  public static com.google.protobuf.Internal.EnumVerifier 
      internalGetVerifier() {
    return LogRecordFlagsVerifier.INSTANCE;
  }

  private static final class LogRecordFlagsVerifier implements 
       com.google.protobuf.Internal.EnumVerifier { 
          static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new LogRecordFlagsVerifier();
          @java.lang.Override
          public boolean isInRange(int number) {
            return LogRecordFlags.forNumber(number) != null;
          }
        };

  private final int value;

  private LogRecordFlags(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:opentelemetry.proto.logs.v1.LogRecordFlags)
}
