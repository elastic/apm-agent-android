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
 * Possible values for LogRecord.SeverityNumber.
 * </pre>
 *
 * Protobuf enum {@code opentelemetry.proto.logs.v1.SeverityNumber}
 */
public enum SeverityNumber
    implements com.google.protobuf.Internal.EnumLite {
  /**
   * <pre>
   * UNSPECIFIED is the default SeverityNumber, it MUST NOT be used.
   * </pre>
   *
   * <code>SEVERITY_NUMBER_UNSPECIFIED = 0;</code>
   */
  SEVERITY_NUMBER_UNSPECIFIED(0),
  /**
   * <code>SEVERITY_NUMBER_TRACE = 1;</code>
   */
  SEVERITY_NUMBER_TRACE(1),
  /**
   * <code>SEVERITY_NUMBER_TRACE2 = 2;</code>
   */
  SEVERITY_NUMBER_TRACE2(2),
  /**
   * <code>SEVERITY_NUMBER_TRACE3 = 3;</code>
   */
  SEVERITY_NUMBER_TRACE3(3),
  /**
   * <code>SEVERITY_NUMBER_TRACE4 = 4;</code>
   */
  SEVERITY_NUMBER_TRACE4(4),
  /**
   * <code>SEVERITY_NUMBER_DEBUG = 5;</code>
   */
  SEVERITY_NUMBER_DEBUG(5),
  /**
   * <code>SEVERITY_NUMBER_DEBUG2 = 6;</code>
   */
  SEVERITY_NUMBER_DEBUG2(6),
  /**
   * <code>SEVERITY_NUMBER_DEBUG3 = 7;</code>
   */
  SEVERITY_NUMBER_DEBUG3(7),
  /**
   * <code>SEVERITY_NUMBER_DEBUG4 = 8;</code>
   */
  SEVERITY_NUMBER_DEBUG4(8),
  /**
   * <code>SEVERITY_NUMBER_INFO = 9;</code>
   */
  SEVERITY_NUMBER_INFO(9),
  /**
   * <code>SEVERITY_NUMBER_INFO2 = 10;</code>
   */
  SEVERITY_NUMBER_INFO2(10),
  /**
   * <code>SEVERITY_NUMBER_INFO3 = 11;</code>
   */
  SEVERITY_NUMBER_INFO3(11),
  /**
   * <code>SEVERITY_NUMBER_INFO4 = 12;</code>
   */
  SEVERITY_NUMBER_INFO4(12),
  /**
   * <code>SEVERITY_NUMBER_WARN = 13;</code>
   */
  SEVERITY_NUMBER_WARN(13),
  /**
   * <code>SEVERITY_NUMBER_WARN2 = 14;</code>
   */
  SEVERITY_NUMBER_WARN2(14),
  /**
   * <code>SEVERITY_NUMBER_WARN3 = 15;</code>
   */
  SEVERITY_NUMBER_WARN3(15),
  /**
   * <code>SEVERITY_NUMBER_WARN4 = 16;</code>
   */
  SEVERITY_NUMBER_WARN4(16),
  /**
   * <code>SEVERITY_NUMBER_ERROR = 17;</code>
   */
  SEVERITY_NUMBER_ERROR(17),
  /**
   * <code>SEVERITY_NUMBER_ERROR2 = 18;</code>
   */
  SEVERITY_NUMBER_ERROR2(18),
  /**
   * <code>SEVERITY_NUMBER_ERROR3 = 19;</code>
   */
  SEVERITY_NUMBER_ERROR3(19),
  /**
   * <code>SEVERITY_NUMBER_ERROR4 = 20;</code>
   */
  SEVERITY_NUMBER_ERROR4(20),
  /**
   * <code>SEVERITY_NUMBER_FATAL = 21;</code>
   */
  SEVERITY_NUMBER_FATAL(21),
  /**
   * <code>SEVERITY_NUMBER_FATAL2 = 22;</code>
   */
  SEVERITY_NUMBER_FATAL2(22),
  /**
   * <code>SEVERITY_NUMBER_FATAL3 = 23;</code>
   */
  SEVERITY_NUMBER_FATAL3(23),
  /**
   * <code>SEVERITY_NUMBER_FATAL4 = 24;</code>
   */
  SEVERITY_NUMBER_FATAL4(24),
  UNRECOGNIZED(-1),
  ;

  /**
   * <pre>
   * UNSPECIFIED is the default SeverityNumber, it MUST NOT be used.
   * </pre>
   *
   * <code>SEVERITY_NUMBER_UNSPECIFIED = 0;</code>
   */
  public static final int SEVERITY_NUMBER_UNSPECIFIED_VALUE = 0;
  /**
   * <code>SEVERITY_NUMBER_TRACE = 1;</code>
   */
  public static final int SEVERITY_NUMBER_TRACE_VALUE = 1;
  /**
   * <code>SEVERITY_NUMBER_TRACE2 = 2;</code>
   */
  public static final int SEVERITY_NUMBER_TRACE2_VALUE = 2;
  /**
   * <code>SEVERITY_NUMBER_TRACE3 = 3;</code>
   */
  public static final int SEVERITY_NUMBER_TRACE3_VALUE = 3;
  /**
   * <code>SEVERITY_NUMBER_TRACE4 = 4;</code>
   */
  public static final int SEVERITY_NUMBER_TRACE4_VALUE = 4;
  /**
   * <code>SEVERITY_NUMBER_DEBUG = 5;</code>
   */
  public static final int SEVERITY_NUMBER_DEBUG_VALUE = 5;
  /**
   * <code>SEVERITY_NUMBER_DEBUG2 = 6;</code>
   */
  public static final int SEVERITY_NUMBER_DEBUG2_VALUE = 6;
  /**
   * <code>SEVERITY_NUMBER_DEBUG3 = 7;</code>
   */
  public static final int SEVERITY_NUMBER_DEBUG3_VALUE = 7;
  /**
   * <code>SEVERITY_NUMBER_DEBUG4 = 8;</code>
   */
  public static final int SEVERITY_NUMBER_DEBUG4_VALUE = 8;
  /**
   * <code>SEVERITY_NUMBER_INFO = 9;</code>
   */
  public static final int SEVERITY_NUMBER_INFO_VALUE = 9;
  /**
   * <code>SEVERITY_NUMBER_INFO2 = 10;</code>
   */
  public static final int SEVERITY_NUMBER_INFO2_VALUE = 10;
  /**
   * <code>SEVERITY_NUMBER_INFO3 = 11;</code>
   */
  public static final int SEVERITY_NUMBER_INFO3_VALUE = 11;
  /**
   * <code>SEVERITY_NUMBER_INFO4 = 12;</code>
   */
  public static final int SEVERITY_NUMBER_INFO4_VALUE = 12;
  /**
   * <code>SEVERITY_NUMBER_WARN = 13;</code>
   */
  public static final int SEVERITY_NUMBER_WARN_VALUE = 13;
  /**
   * <code>SEVERITY_NUMBER_WARN2 = 14;</code>
   */
  public static final int SEVERITY_NUMBER_WARN2_VALUE = 14;
  /**
   * <code>SEVERITY_NUMBER_WARN3 = 15;</code>
   */
  public static final int SEVERITY_NUMBER_WARN3_VALUE = 15;
  /**
   * <code>SEVERITY_NUMBER_WARN4 = 16;</code>
   */
  public static final int SEVERITY_NUMBER_WARN4_VALUE = 16;
  /**
   * <code>SEVERITY_NUMBER_ERROR = 17;</code>
   */
  public static final int SEVERITY_NUMBER_ERROR_VALUE = 17;
  /**
   * <code>SEVERITY_NUMBER_ERROR2 = 18;</code>
   */
  public static final int SEVERITY_NUMBER_ERROR2_VALUE = 18;
  /**
   * <code>SEVERITY_NUMBER_ERROR3 = 19;</code>
   */
  public static final int SEVERITY_NUMBER_ERROR3_VALUE = 19;
  /**
   * <code>SEVERITY_NUMBER_ERROR4 = 20;</code>
   */
  public static final int SEVERITY_NUMBER_ERROR4_VALUE = 20;
  /**
   * <code>SEVERITY_NUMBER_FATAL = 21;</code>
   */
  public static final int SEVERITY_NUMBER_FATAL_VALUE = 21;
  /**
   * <code>SEVERITY_NUMBER_FATAL2 = 22;</code>
   */
  public static final int SEVERITY_NUMBER_FATAL2_VALUE = 22;
  /**
   * <code>SEVERITY_NUMBER_FATAL3 = 23;</code>
   */
  public static final int SEVERITY_NUMBER_FATAL3_VALUE = 23;
  /**
   * <code>SEVERITY_NUMBER_FATAL4 = 24;</code>
   */
  public static final int SEVERITY_NUMBER_FATAL4_VALUE = 24;


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
  public static SeverityNumber valueOf(int value) {
    return forNumber(value);
  }

  public static SeverityNumber forNumber(int value) {
    switch (value) {
      case 0: return SEVERITY_NUMBER_UNSPECIFIED;
      case 1: return SEVERITY_NUMBER_TRACE;
      case 2: return SEVERITY_NUMBER_TRACE2;
      case 3: return SEVERITY_NUMBER_TRACE3;
      case 4: return SEVERITY_NUMBER_TRACE4;
      case 5: return SEVERITY_NUMBER_DEBUG;
      case 6: return SEVERITY_NUMBER_DEBUG2;
      case 7: return SEVERITY_NUMBER_DEBUG3;
      case 8: return SEVERITY_NUMBER_DEBUG4;
      case 9: return SEVERITY_NUMBER_INFO;
      case 10: return SEVERITY_NUMBER_INFO2;
      case 11: return SEVERITY_NUMBER_INFO3;
      case 12: return SEVERITY_NUMBER_INFO4;
      case 13: return SEVERITY_NUMBER_WARN;
      case 14: return SEVERITY_NUMBER_WARN2;
      case 15: return SEVERITY_NUMBER_WARN3;
      case 16: return SEVERITY_NUMBER_WARN4;
      case 17: return SEVERITY_NUMBER_ERROR;
      case 18: return SEVERITY_NUMBER_ERROR2;
      case 19: return SEVERITY_NUMBER_ERROR3;
      case 20: return SEVERITY_NUMBER_ERROR4;
      case 21: return SEVERITY_NUMBER_FATAL;
      case 22: return SEVERITY_NUMBER_FATAL2;
      case 23: return SEVERITY_NUMBER_FATAL3;
      case 24: return SEVERITY_NUMBER_FATAL4;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<SeverityNumber>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      SeverityNumber> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<SeverityNumber>() {
          @java.lang.Override
          public SeverityNumber findValueByNumber(int number) {
            return SeverityNumber.forNumber(number);
          }
        };

  public static com.google.protobuf.Internal.EnumVerifier 
      internalGetVerifier() {
    return SeverityNumberVerifier.INSTANCE;
  }

  private static final class SeverityNumberVerifier implements 
       com.google.protobuf.Internal.EnumVerifier { 
          static final com.google.protobuf.Internal.EnumVerifier           INSTANCE = new SeverityNumberVerifier();
          @java.lang.Override
          public boolean isInRange(int number) {
            return SeverityNumber.forNumber(number) != null;
          }
        };

  private final int value;

  private SeverityNumber(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:opentelemetry.proto.logs.v1.SeverityNumber)
}
