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
 * A log record according to OpenTelemetry Log Data Model:
 * https://github.com/open-telemetry/oteps/blob/main/text/logs/0097-log-data-model.md
 * </pre>
 *
 * Protobuf type {@code opentelemetry.proto.logs.v1.LogRecord}
 */
public  final class LogRecord extends
    com.google.protobuf.GeneratedMessageLite<
        LogRecord, LogRecord.Builder> implements
    // @@protoc_insertion_point(message_implements:opentelemetry.proto.logs.v1.LogRecord)
    LogRecordOrBuilder {
  private LogRecord() {
    severityText_ = "";
    attributes_ = emptyProtobufList();
    traceId_ = com.google.protobuf.ByteString.EMPTY;
    spanId_ = com.google.protobuf.ByteString.EMPTY;
  }
  public static final int TIME_UNIX_NANO_FIELD_NUMBER = 1;
  private long timeUnixNano_;
  /**
   * <pre>
   * time_unix_nano is the time when the event occurred.
   * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
   * Value of 0 indicates unknown or missing timestamp.
   * </pre>
   *
   * <code>fixed64 time_unix_nano = 1;</code>
   * @return The timeUnixNano.
   */
  @java.lang.Override
  public long getTimeUnixNano() {
    return timeUnixNano_;
  }
  /**
   * <pre>
   * time_unix_nano is the time when the event occurred.
   * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
   * Value of 0 indicates unknown or missing timestamp.
   * </pre>
   *
   * <code>fixed64 time_unix_nano = 1;</code>
   * @param value The timeUnixNano to set.
   */
  private void setTimeUnixNano(long value) {
    
    timeUnixNano_ = value;
  }
  /**
   * <pre>
   * time_unix_nano is the time when the event occurred.
   * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
   * Value of 0 indicates unknown or missing timestamp.
   * </pre>
   *
   * <code>fixed64 time_unix_nano = 1;</code>
   */
  private void clearTimeUnixNano() {
    
    timeUnixNano_ = 0L;
  }

  public static final int OBSERVED_TIME_UNIX_NANO_FIELD_NUMBER = 11;
  private long observedTimeUnixNano_;
  /**
   * <pre>
   * Time when the event was observed by the collection system.
   * For events that originate in OpenTelemetry (e.g. using OpenTelemetry Logging SDK)
   * this timestamp is typically set at the generation time and is equal to Timestamp.
   * For events originating externally and collected by OpenTelemetry (e.g. using
   * Collector) this is the time when OpenTelemetry's code observed the event measured
   * by the clock of the OpenTelemetry code. This field MUST be set once the event is
   * observed by OpenTelemetry.
   * For converting OpenTelemetry log data to formats that support only one timestamp or
   * when receiving OpenTelemetry log data by recipients that support only one timestamp
   * internally the following logic is recommended:
   *   - Use time_unix_nano if it is present, otherwise use observed_time_unix_nano.
   * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
   * Value of 0 indicates unknown or missing timestamp.
   * </pre>
   *
   * <code>fixed64 observed_time_unix_nano = 11;</code>
   * @return The observedTimeUnixNano.
   */
  @java.lang.Override
  public long getObservedTimeUnixNano() {
    return observedTimeUnixNano_;
  }
  /**
   * <pre>
   * Time when the event was observed by the collection system.
   * For events that originate in OpenTelemetry (e.g. using OpenTelemetry Logging SDK)
   * this timestamp is typically set at the generation time and is equal to Timestamp.
   * For events originating externally and collected by OpenTelemetry (e.g. using
   * Collector) this is the time when OpenTelemetry's code observed the event measured
   * by the clock of the OpenTelemetry code. This field MUST be set once the event is
   * observed by OpenTelemetry.
   * For converting OpenTelemetry log data to formats that support only one timestamp or
   * when receiving OpenTelemetry log data by recipients that support only one timestamp
   * internally the following logic is recommended:
   *   - Use time_unix_nano if it is present, otherwise use observed_time_unix_nano.
   * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
   * Value of 0 indicates unknown or missing timestamp.
   * </pre>
   *
   * <code>fixed64 observed_time_unix_nano = 11;</code>
   * @param value The observedTimeUnixNano to set.
   */
  private void setObservedTimeUnixNano(long value) {
    
    observedTimeUnixNano_ = value;
  }
  /**
   * <pre>
   * Time when the event was observed by the collection system.
   * For events that originate in OpenTelemetry (e.g. using OpenTelemetry Logging SDK)
   * this timestamp is typically set at the generation time and is equal to Timestamp.
   * For events originating externally and collected by OpenTelemetry (e.g. using
   * Collector) this is the time when OpenTelemetry's code observed the event measured
   * by the clock of the OpenTelemetry code. This field MUST be set once the event is
   * observed by OpenTelemetry.
   * For converting OpenTelemetry log data to formats that support only one timestamp or
   * when receiving OpenTelemetry log data by recipients that support only one timestamp
   * internally the following logic is recommended:
   *   - Use time_unix_nano if it is present, otherwise use observed_time_unix_nano.
   * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
   * Value of 0 indicates unknown or missing timestamp.
   * </pre>
   *
   * <code>fixed64 observed_time_unix_nano = 11;</code>
   */
  private void clearObservedTimeUnixNano() {
    
    observedTimeUnixNano_ = 0L;
  }

  public static final int SEVERITY_NUMBER_FIELD_NUMBER = 2;
  private int severityNumber_;
  /**
   * <pre>
   * Numerical value of the severity, normalized to values described in Log Data Model.
   * [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
   * @return The enum numeric value on the wire for severityNumber.
   */
  @java.lang.Override
  public int getSeverityNumberValue() {
    return severityNumber_;
  }
  /**
   * <pre>
   * Numerical value of the severity, normalized to values described in Log Data Model.
   * [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
   * @return The severityNumber.
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber getSeverityNumber() {
    co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber result = co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber.forNumber(severityNumber_);
    return result == null ? co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber.UNRECOGNIZED : result;
  }
  /**
   * <pre>
   * Numerical value of the severity, normalized to values described in Log Data Model.
   * [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
   * @param value The enum numeric value on the wire for severityNumber to set.
   */
  private void setSeverityNumberValue(int value) {
      severityNumber_ = value;
  }
  /**
   * <pre>
   * Numerical value of the severity, normalized to values described in Log Data Model.
   * [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
   * @param value The severityNumber to set.
   */
  private void setSeverityNumber(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber value) {
    severityNumber_ = value.getNumber();
    
  }
  /**
   * <pre>
   * Numerical value of the severity, normalized to values described in Log Data Model.
   * [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
   */
  private void clearSeverityNumber() {
    
    severityNumber_ = 0;
  }

  public static final int SEVERITY_TEXT_FIELD_NUMBER = 3;
  private java.lang.String severityText_;
  /**
   * <pre>
   * The severity text (also known as log level). The original string representation as
   * it is known at the source. [Optional].
   * </pre>
   *
   * <code>string severity_text = 3;</code>
   * @return The severityText.
   */
  @java.lang.Override
  public java.lang.String getSeverityText() {
    return severityText_;
  }
  /**
   * <pre>
   * The severity text (also known as log level). The original string representation as
   * it is known at the source. [Optional].
   * </pre>
   *
   * <code>string severity_text = 3;</code>
   * @return The bytes for severityText.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSeverityTextBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(severityText_);
  }
  /**
   * <pre>
   * The severity text (also known as log level). The original string representation as
   * it is known at the source. [Optional].
   * </pre>
   *
   * <code>string severity_text = 3;</code>
   * @param value The severityText to set.
   */
  private void setSeverityText(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    severityText_ = value;
  }
  /**
   * <pre>
   * The severity text (also known as log level). The original string representation as
   * it is known at the source. [Optional].
   * </pre>
   *
   * <code>string severity_text = 3;</code>
   */
  private void clearSeverityText() {
    
    severityText_ = getDefaultInstance().getSeverityText();
  }
  /**
   * <pre>
   * The severity text (also known as log level). The original string representation as
   * it is known at the source. [Optional].
   * </pre>
   *
   * <code>string severity_text = 3;</code>
   * @param value The bytes for severityText to set.
   */
  private void setSeverityTextBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    severityText_ = value.toStringUtf8();
    
  }

  public static final int BODY_FIELD_NUMBER = 5;
  private co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue body_;
  /**
   * <pre>
   * A value containing the body of the log record. Can be for example a human-readable
   * string message (including multi-line) describing the event in a free form or it can
   * be a structured data composed of arrays and maps of other values. [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
   */
  @java.lang.Override
  public boolean hasBody() {
    return body_ != null;
  }
  /**
   * <pre>
   * A value containing the body of the log record. Can be for example a human-readable
   * string message (including multi-line) describing the event in a free form or it can
   * be a structured data composed of arrays and maps of other values. [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue getBody() {
    return body_ == null ? co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.getDefaultInstance() : body_;
  }
  /**
   * <pre>
   * A value containing the body of the log record. Can be for example a human-readable
   * string message (including multi-line) describing the event in a free form or it can
   * be a structured data composed of arrays and maps of other values. [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
   */
  private void setBody(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
    value.getClass();
  body_ = value;
    
    }
  /**
   * <pre>
   * A value containing the body of the log record. Can be for example a human-readable
   * string message (including multi-line) describing the event in a free form or it can
   * be a structured data composed of arrays and maps of other values. [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeBody(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
    value.getClass();
  if (body_ != null &&
        body_ != co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.getDefaultInstance()) {
      body_ =
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.newBuilder(body_).mergeFrom(value).buildPartial();
    } else {
      body_ = value;
    }
    
  }
  /**
   * <pre>
   * A value containing the body of the log record. Can be for example a human-readable
   * string message (including multi-line) describing the event in a free form or it can
   * be a structured data composed of arrays and maps of other values. [Optional].
   * </pre>
   *
   * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
   */
  private void clearBody() {  body_ = null;
    
  }

  public static final int ATTRIBUTES_FIELD_NUMBER = 6;
  private com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> attributes_;
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  @java.lang.Override
  public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> getAttributesList() {
    return attributes_;
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  public java.util.List<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueOrBuilder> 
      getAttributesOrBuilderList() {
    return attributes_;
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  @java.lang.Override
  public int getAttributesCount() {
    return attributes_.size();
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  @java.lang.Override
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getAttributes(int index) {
    return attributes_.get(index);
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValueOrBuilder getAttributesOrBuilder(
      int index) {
    return attributes_.get(index);
  }
  private void ensureAttributesIsMutable() {
    com.google.protobuf.Internal.ProtobufList<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> tmp = attributes_;
    if (!tmp.isModifiable()) {
      attributes_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  private void setAttributes(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.set(index, value);
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  private void addAttributes(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.add(value);
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  private void addAttributes(
      int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
    value.getClass();
  ensureAttributesIsMutable();
    attributes_.add(index, value);
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  private void addAllAttributes(
      java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values) {
    ensureAttributesIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, attributes_);
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  private void clearAttributes() {
    attributes_ = emptyProtobufList();
  }
  /**
   * <pre>
   * Additional attributes that describe the specific event occurrence. [Optional].
   * Attribute keys MUST be unique (it is not allowed to have more than one
   * attribute with the same key).
   * </pre>
   *
   * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
   */
  private void removeAttributes(int index) {
    ensureAttributesIsMutable();
    attributes_.remove(index);
  }

  public static final int DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER = 7;
  private int droppedAttributesCount_;
  /**
   * <code>uint32 dropped_attributes_count = 7;</code>
   * @return The droppedAttributesCount.
   */
  @java.lang.Override
  public int getDroppedAttributesCount() {
    return droppedAttributesCount_;
  }
  /**
   * <code>uint32 dropped_attributes_count = 7;</code>
   * @param value The droppedAttributesCount to set.
   */
  private void setDroppedAttributesCount(int value) {
    
    droppedAttributesCount_ = value;
  }
  /**
   * <code>uint32 dropped_attributes_count = 7;</code>
   */
  private void clearDroppedAttributesCount() {
    
    droppedAttributesCount_ = 0;
  }

  public static final int FLAGS_FIELD_NUMBER = 8;
  private int flags_;
  /**
   * <pre>
   * Flags, a bit field. 8 least significant bits are the trace flags as
   * defined in W3C Trace Context specification. 24 most significant bits are reserved
   * and must be set to 0. Readers must not assume that 24 most significant bits
   * will be zero and must correctly mask the bits when reading 8-bit trace flag (use
   * flags &amp; TRACE_FLAGS_MASK). [Optional].
   * </pre>
   *
   * <code>fixed32 flags = 8;</code>
   * @return The flags.
   */
  @java.lang.Override
  public int getFlags() {
    return flags_;
  }
  /**
   * <pre>
   * Flags, a bit field. 8 least significant bits are the trace flags as
   * defined in W3C Trace Context specification. 24 most significant bits are reserved
   * and must be set to 0. Readers must not assume that 24 most significant bits
   * will be zero and must correctly mask the bits when reading 8-bit trace flag (use
   * flags &amp; TRACE_FLAGS_MASK). [Optional].
   * </pre>
   *
   * <code>fixed32 flags = 8;</code>
   * @param value The flags to set.
   */
  private void setFlags(int value) {
    
    flags_ = value;
  }
  /**
   * <pre>
   * Flags, a bit field. 8 least significant bits are the trace flags as
   * defined in W3C Trace Context specification. 24 most significant bits are reserved
   * and must be set to 0. Readers must not assume that 24 most significant bits
   * will be zero and must correctly mask the bits when reading 8-bit trace flag (use
   * flags &amp; TRACE_FLAGS_MASK). [Optional].
   * </pre>
   *
   * <code>fixed32 flags = 8;</code>
   */
  private void clearFlags() {
    
    flags_ = 0;
  }

  public static final int TRACE_ID_FIELD_NUMBER = 9;
  private com.google.protobuf.ByteString traceId_;
  /**
   * <pre>
   * A unique identifier for a trace. All logs from the same trace share
   * the same `trace_id`. The ID is a 16-byte array. An ID with all zeroes OR
   * of length other than 16 bytes is considered invalid (empty string in OTLP/JSON
   * is zero-length and thus is also invalid).
   * This field is optional.
   * The receivers SHOULD assume that the log record is not associated with a
   * trace if any of the following is true:
   *   - the field is not present,
   *   - the field contains an invalid value.
   * </pre>
   *
   * <code>bytes trace_id = 9;</code>
   * @return The traceId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getTraceId() {
    return traceId_;
  }
  /**
   * <pre>
   * A unique identifier for a trace. All logs from the same trace share
   * the same `trace_id`. The ID is a 16-byte array. An ID with all zeroes OR
   * of length other than 16 bytes is considered invalid (empty string in OTLP/JSON
   * is zero-length and thus is also invalid).
   * This field is optional.
   * The receivers SHOULD assume that the log record is not associated with a
   * trace if any of the following is true:
   *   - the field is not present,
   *   - the field contains an invalid value.
   * </pre>
   *
   * <code>bytes trace_id = 9;</code>
   * @param value The traceId to set.
   */
  private void setTraceId(com.google.protobuf.ByteString value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    traceId_ = value;
  }
  /**
   * <pre>
   * A unique identifier for a trace. All logs from the same trace share
   * the same `trace_id`. The ID is a 16-byte array. An ID with all zeroes OR
   * of length other than 16 bytes is considered invalid (empty string in OTLP/JSON
   * is zero-length and thus is also invalid).
   * This field is optional.
   * The receivers SHOULD assume that the log record is not associated with a
   * trace if any of the following is true:
   *   - the field is not present,
   *   - the field contains an invalid value.
   * </pre>
   *
   * <code>bytes trace_id = 9;</code>
   */
  private void clearTraceId() {
    
    traceId_ = getDefaultInstance().getTraceId();
  }

  public static final int SPAN_ID_FIELD_NUMBER = 10;
  private com.google.protobuf.ByteString spanId_;
  /**
   * <pre>
   * A unique identifier for a span within a trace, assigned when the span
   * is created. The ID is an 8-byte array. An ID with all zeroes OR of length
   * other than 8 bytes is considered invalid (empty string in OTLP/JSON
   * is zero-length and thus is also invalid).
   * This field is optional. If the sender specifies a valid span_id then it SHOULD also
   * specify a valid trace_id.
   * The receivers SHOULD assume that the log record is not associated with a
   * span if any of the following is true:
   *   - the field is not present,
   *   - the field contains an invalid value.
   * </pre>
   *
   * <code>bytes span_id = 10;</code>
   * @return The spanId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getSpanId() {
    return spanId_;
  }
  /**
   * <pre>
   * A unique identifier for a span within a trace, assigned when the span
   * is created. The ID is an 8-byte array. An ID with all zeroes OR of length
   * other than 8 bytes is considered invalid (empty string in OTLP/JSON
   * is zero-length and thus is also invalid).
   * This field is optional. If the sender specifies a valid span_id then it SHOULD also
   * specify a valid trace_id.
   * The receivers SHOULD assume that the log record is not associated with a
   * span if any of the following is true:
   *   - the field is not present,
   *   - the field contains an invalid value.
   * </pre>
   *
   * <code>bytes span_id = 10;</code>
   * @param value The spanId to set.
   */
  private void setSpanId(com.google.protobuf.ByteString value) {
    java.lang.Class<?> valueClass = value.getClass();
  
    spanId_ = value;
  }
  /**
   * <pre>
   * A unique identifier for a span within a trace, assigned when the span
   * is created. The ID is an 8-byte array. An ID with all zeroes OR of length
   * other than 8 bytes is considered invalid (empty string in OTLP/JSON
   * is zero-length and thus is also invalid).
   * This field is optional. If the sender specifies a valid span_id then it SHOULD also
   * specify a valid trace_id.
   * The receivers SHOULD assume that the log record is not associated with a
   * span if any of the following is true:
   *   - the field is not present,
   *   - the field contains an invalid value.
   * </pre>
   *
   * <code>bytes span_id = 10;</code>
   */
  private void clearSpanId() {
    
    spanId_ = getDefaultInstance().getSpanId();
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * <pre>
   * A log record according to OpenTelemetry Log Data Model:
   * https://github.com/open-telemetry/oteps/blob/main/text/logs/0097-log-data-model.md
   * </pre>
   *
   * Protobuf type {@code opentelemetry.proto.logs.v1.LogRecord}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord, Builder> implements
      // @@protoc_insertion_point(builder_implements:opentelemetry.proto.logs.v1.LogRecord)
      co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecordOrBuilder {
    // Construct using co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <pre>
     * time_unix_nano is the time when the event occurred.
     * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
     * Value of 0 indicates unknown or missing timestamp.
     * </pre>
     *
     * <code>fixed64 time_unix_nano = 1;</code>
     * @return The timeUnixNano.
     */
    @java.lang.Override
    public long getTimeUnixNano() {
      return instance.getTimeUnixNano();
    }
    /**
     * <pre>
     * time_unix_nano is the time when the event occurred.
     * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
     * Value of 0 indicates unknown or missing timestamp.
     * </pre>
     *
     * <code>fixed64 time_unix_nano = 1;</code>
     * @param value The timeUnixNano to set.
     * @return This builder for chaining.
     */
    public Builder setTimeUnixNano(long value) {
      copyOnWrite();
      instance.setTimeUnixNano(value);
      return this;
    }
    /**
     * <pre>
     * time_unix_nano is the time when the event occurred.
     * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
     * Value of 0 indicates unknown or missing timestamp.
     * </pre>
     *
     * <code>fixed64 time_unix_nano = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearTimeUnixNano() {
      copyOnWrite();
      instance.clearTimeUnixNano();
      return this;
    }

    /**
     * <pre>
     * Time when the event was observed by the collection system.
     * For events that originate in OpenTelemetry (e.g. using OpenTelemetry Logging SDK)
     * this timestamp is typically set at the generation time and is equal to Timestamp.
     * For events originating externally and collected by OpenTelemetry (e.g. using
     * Collector) this is the time when OpenTelemetry's code observed the event measured
     * by the clock of the OpenTelemetry code. This field MUST be set once the event is
     * observed by OpenTelemetry.
     * For converting OpenTelemetry log data to formats that support only one timestamp or
     * when receiving OpenTelemetry log data by recipients that support only one timestamp
     * internally the following logic is recommended:
     *   - Use time_unix_nano if it is present, otherwise use observed_time_unix_nano.
     * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
     * Value of 0 indicates unknown or missing timestamp.
     * </pre>
     *
     * <code>fixed64 observed_time_unix_nano = 11;</code>
     * @return The observedTimeUnixNano.
     */
    @java.lang.Override
    public long getObservedTimeUnixNano() {
      return instance.getObservedTimeUnixNano();
    }
    /**
     * <pre>
     * Time when the event was observed by the collection system.
     * For events that originate in OpenTelemetry (e.g. using OpenTelemetry Logging SDK)
     * this timestamp is typically set at the generation time and is equal to Timestamp.
     * For events originating externally and collected by OpenTelemetry (e.g. using
     * Collector) this is the time when OpenTelemetry's code observed the event measured
     * by the clock of the OpenTelemetry code. This field MUST be set once the event is
     * observed by OpenTelemetry.
     * For converting OpenTelemetry log data to formats that support only one timestamp or
     * when receiving OpenTelemetry log data by recipients that support only one timestamp
     * internally the following logic is recommended:
     *   - Use time_unix_nano if it is present, otherwise use observed_time_unix_nano.
     * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
     * Value of 0 indicates unknown or missing timestamp.
     * </pre>
     *
     * <code>fixed64 observed_time_unix_nano = 11;</code>
     * @param value The observedTimeUnixNano to set.
     * @return This builder for chaining.
     */
    public Builder setObservedTimeUnixNano(long value) {
      copyOnWrite();
      instance.setObservedTimeUnixNano(value);
      return this;
    }
    /**
     * <pre>
     * Time when the event was observed by the collection system.
     * For events that originate in OpenTelemetry (e.g. using OpenTelemetry Logging SDK)
     * this timestamp is typically set at the generation time and is equal to Timestamp.
     * For events originating externally and collected by OpenTelemetry (e.g. using
     * Collector) this is the time when OpenTelemetry's code observed the event measured
     * by the clock of the OpenTelemetry code. This field MUST be set once the event is
     * observed by OpenTelemetry.
     * For converting OpenTelemetry log data to formats that support only one timestamp or
     * when receiving OpenTelemetry log data by recipients that support only one timestamp
     * internally the following logic is recommended:
     *   - Use time_unix_nano if it is present, otherwise use observed_time_unix_nano.
     * Value is UNIX Epoch time in nanoseconds since 00:00:00 UTC on 1 January 1970.
     * Value of 0 indicates unknown or missing timestamp.
     * </pre>
     *
     * <code>fixed64 observed_time_unix_nano = 11;</code>
     * @return This builder for chaining.
     */
    public Builder clearObservedTimeUnixNano() {
      copyOnWrite();
      instance.clearObservedTimeUnixNano();
      return this;
    }

    /**
     * <pre>
     * Numerical value of the severity, normalized to values described in Log Data Model.
     * [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
     * @return The enum numeric value on the wire for severityNumber.
     */
    @java.lang.Override
    public int getSeverityNumberValue() {
      return instance.getSeverityNumberValue();
    }
    /**
     * <pre>
     * Numerical value of the severity, normalized to values described in Log Data Model.
     * [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
     * @param value The severityNumber to set.
     * @return This builder for chaining.
     */
    public Builder setSeverityNumberValue(int value) {
      copyOnWrite();
      instance.setSeverityNumberValue(value);
      return this;
    }
    /**
     * <pre>
     * Numerical value of the severity, normalized to values described in Log Data Model.
     * [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
     * @return The severityNumber.
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber getSeverityNumber() {
      return instance.getSeverityNumber();
    }
    /**
     * <pre>
     * Numerical value of the severity, normalized to values described in Log Data Model.
     * [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
     * @param value The enum numeric value on the wire for severityNumber to set.
     * @return This builder for chaining.
     */
    public Builder setSeverityNumber(co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber value) {
      copyOnWrite();
      instance.setSeverityNumber(value);
      return this;
    }
    /**
     * <pre>
     * Numerical value of the severity, normalized to values described in Log Data Model.
     * [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.logs.v1.SeverityNumber severity_number = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearSeverityNumber() {
      copyOnWrite();
      instance.clearSeverityNumber();
      return this;
    }

    /**
     * <pre>
     * The severity text (also known as log level). The original string representation as
     * it is known at the source. [Optional].
     * </pre>
     *
     * <code>string severity_text = 3;</code>
     * @return The severityText.
     */
    @java.lang.Override
    public java.lang.String getSeverityText() {
      return instance.getSeverityText();
    }
    /**
     * <pre>
     * The severity text (also known as log level). The original string representation as
     * it is known at the source. [Optional].
     * </pre>
     *
     * <code>string severity_text = 3;</code>
     * @return The bytes for severityText.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSeverityTextBytes() {
      return instance.getSeverityTextBytes();
    }
    /**
     * <pre>
     * The severity text (also known as log level). The original string representation as
     * it is known at the source. [Optional].
     * </pre>
     *
     * <code>string severity_text = 3;</code>
     * @param value The severityText to set.
     * @return This builder for chaining.
     */
    public Builder setSeverityText(
        java.lang.String value) {
      copyOnWrite();
      instance.setSeverityText(value);
      return this;
    }
    /**
     * <pre>
     * The severity text (also known as log level). The original string representation as
     * it is known at the source. [Optional].
     * </pre>
     *
     * <code>string severity_text = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearSeverityText() {
      copyOnWrite();
      instance.clearSeverityText();
      return this;
    }
    /**
     * <pre>
     * The severity text (also known as log level). The original string representation as
     * it is known at the source. [Optional].
     * </pre>
     *
     * <code>string severity_text = 3;</code>
     * @param value The bytes for severityText to set.
     * @return This builder for chaining.
     */
    public Builder setSeverityTextBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSeverityTextBytes(value);
      return this;
    }

    /**
     * <pre>
     * A value containing the body of the log record. Can be for example a human-readable
     * string message (including multi-line) describing the event in a free form or it can
     * be a structured data composed of arrays and maps of other values. [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
     */
    @java.lang.Override
    public boolean hasBody() {
      return instance.hasBody();
    }
    /**
     * <pre>
     * A value containing the body of the log record. Can be for example a human-readable
     * string message (including multi-line) describing the event in a free form or it can
     * be a structured data composed of arrays and maps of other values. [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue getBody() {
      return instance.getBody();
    }
    /**
     * <pre>
     * A value containing the body of the log record. Can be for example a human-readable
     * string message (including multi-line) describing the event in a free form or it can
     * be a structured data composed of arrays and maps of other values. [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
     */
    public Builder setBody(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
      copyOnWrite();
      instance.setBody(value);
      return this;
      }
    /**
     * <pre>
     * A value containing the body of the log record. Can be for example a human-readable
     * string message (including multi-line) describing the event in a free form or it can
     * be a structured data composed of arrays and maps of other values. [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
     */
    public Builder setBody(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue.Builder builderForValue) {
      copyOnWrite();
      instance.setBody(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * A value containing the body of the log record. Can be for example a human-readable
     * string message (including multi-line) describing the event in a free form or it can
     * be a structured data composed of arrays and maps of other values. [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
     */
    public Builder mergeBody(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue value) {
      copyOnWrite();
      instance.mergeBody(value);
      return this;
    }
    /**
     * <pre>
     * A value containing the body of the log record. Can be for example a human-readable
     * string message (including multi-line) describing the event in a free form or it can
     * be a structured data composed of arrays and maps of other values. [Optional].
     * </pre>
     *
     * <code>.opentelemetry.proto.common.v1.AnyValue body = 5;</code>
     */
    public Builder clearBody() {  copyOnWrite();
      instance.clearBody();
      return this;
    }

    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    @java.lang.Override
    public java.util.List<co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> getAttributesList() {
      return java.util.Collections.unmodifiableList(
          instance.getAttributesList());
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    @java.lang.Override
    public int getAttributesCount() {
      return instance.getAttributesCount();
    }/**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    @java.lang.Override
    public co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue getAttributes(int index) {
      return instance.getAttributes(index);
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder setAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.setAttributes(index, value);
      return this;
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder setAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.setAttributes(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder addAttributes(co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.addAttributes(value);
      return this;
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder addAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue value) {
      copyOnWrite();
      instance.addAttributes(index, value);
      return this;
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder addAttributes(
        co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addAttributes(builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder addAttributes(
        int index, co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.Builder builderForValue) {
      copyOnWrite();
      instance.addAttributes(index,
          builderForValue.build());
      return this;
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder addAllAttributes(
        java.lang.Iterable<? extends co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue> values) {
      copyOnWrite();
      instance.addAllAttributes(values);
      return this;
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder clearAttributes() {
      copyOnWrite();
      instance.clearAttributes();
      return this;
    }
    /**
     * <pre>
     * Additional attributes that describe the specific event occurrence. [Optional].
     * Attribute keys MUST be unique (it is not allowed to have more than one
     * attribute with the same key).
     * </pre>
     *
     * <code>repeated .opentelemetry.proto.common.v1.KeyValue attributes = 6;</code>
     */
    public Builder removeAttributes(int index) {
      copyOnWrite();
      instance.removeAttributes(index);
      return this;
    }

    /**
     * <code>uint32 dropped_attributes_count = 7;</code>
     * @return The droppedAttributesCount.
     */
    @java.lang.Override
    public int getDroppedAttributesCount() {
      return instance.getDroppedAttributesCount();
    }
    /**
     * <code>uint32 dropped_attributes_count = 7;</code>
     * @param value The droppedAttributesCount to set.
     * @return This builder for chaining.
     */
    public Builder setDroppedAttributesCount(int value) {
      copyOnWrite();
      instance.setDroppedAttributesCount(value);
      return this;
    }
    /**
     * <code>uint32 dropped_attributes_count = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearDroppedAttributesCount() {
      copyOnWrite();
      instance.clearDroppedAttributesCount();
      return this;
    }

    /**
     * <pre>
     * Flags, a bit field. 8 least significant bits are the trace flags as
     * defined in W3C Trace Context specification. 24 most significant bits are reserved
     * and must be set to 0. Readers must not assume that 24 most significant bits
     * will be zero and must correctly mask the bits when reading 8-bit trace flag (use
     * flags &amp; TRACE_FLAGS_MASK). [Optional].
     * </pre>
     *
     * <code>fixed32 flags = 8;</code>
     * @return The flags.
     */
    @java.lang.Override
    public int getFlags() {
      return instance.getFlags();
    }
    /**
     * <pre>
     * Flags, a bit field. 8 least significant bits are the trace flags as
     * defined in W3C Trace Context specification. 24 most significant bits are reserved
     * and must be set to 0. Readers must not assume that 24 most significant bits
     * will be zero and must correctly mask the bits when reading 8-bit trace flag (use
     * flags &amp; TRACE_FLAGS_MASK). [Optional].
     * </pre>
     *
     * <code>fixed32 flags = 8;</code>
     * @param value The flags to set.
     * @return This builder for chaining.
     */
    public Builder setFlags(int value) {
      copyOnWrite();
      instance.setFlags(value);
      return this;
    }
    /**
     * <pre>
     * Flags, a bit field. 8 least significant bits are the trace flags as
     * defined in W3C Trace Context specification. 24 most significant bits are reserved
     * and must be set to 0. Readers must not assume that 24 most significant bits
     * will be zero and must correctly mask the bits when reading 8-bit trace flag (use
     * flags &amp; TRACE_FLAGS_MASK). [Optional].
     * </pre>
     *
     * <code>fixed32 flags = 8;</code>
     * @return This builder for chaining.
     */
    public Builder clearFlags() {
      copyOnWrite();
      instance.clearFlags();
      return this;
    }

    /**
     * <pre>
     * A unique identifier for a trace. All logs from the same trace share
     * the same `trace_id`. The ID is a 16-byte array. An ID with all zeroes OR
     * of length other than 16 bytes is considered invalid (empty string in OTLP/JSON
     * is zero-length and thus is also invalid).
     * This field is optional.
     * The receivers SHOULD assume that the log record is not associated with a
     * trace if any of the following is true:
     *   - the field is not present,
     *   - the field contains an invalid value.
     * </pre>
     *
     * <code>bytes trace_id = 9;</code>
     * @return The traceId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getTraceId() {
      return instance.getTraceId();
    }
    /**
     * <pre>
     * A unique identifier for a trace. All logs from the same trace share
     * the same `trace_id`. The ID is a 16-byte array. An ID with all zeroes OR
     * of length other than 16 bytes is considered invalid (empty string in OTLP/JSON
     * is zero-length and thus is also invalid).
     * This field is optional.
     * The receivers SHOULD assume that the log record is not associated with a
     * trace if any of the following is true:
     *   - the field is not present,
     *   - the field contains an invalid value.
     * </pre>
     *
     * <code>bytes trace_id = 9;</code>
     * @param value The traceId to set.
     * @return This builder for chaining.
     */
    public Builder setTraceId(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setTraceId(value);
      return this;
    }
    /**
     * <pre>
     * A unique identifier for a trace. All logs from the same trace share
     * the same `trace_id`. The ID is a 16-byte array. An ID with all zeroes OR
     * of length other than 16 bytes is considered invalid (empty string in OTLP/JSON
     * is zero-length and thus is also invalid).
     * This field is optional.
     * The receivers SHOULD assume that the log record is not associated with a
     * trace if any of the following is true:
     *   - the field is not present,
     *   - the field contains an invalid value.
     * </pre>
     *
     * <code>bytes trace_id = 9;</code>
     * @return This builder for chaining.
     */
    public Builder clearTraceId() {
      copyOnWrite();
      instance.clearTraceId();
      return this;
    }

    /**
     * <pre>
     * A unique identifier for a span within a trace, assigned when the span
     * is created. The ID is an 8-byte array. An ID with all zeroes OR of length
     * other than 8 bytes is considered invalid (empty string in OTLP/JSON
     * is zero-length and thus is also invalid).
     * This field is optional. If the sender specifies a valid span_id then it SHOULD also
     * specify a valid trace_id.
     * The receivers SHOULD assume that the log record is not associated with a
     * span if any of the following is true:
     *   - the field is not present,
     *   - the field contains an invalid value.
     * </pre>
     *
     * <code>bytes span_id = 10;</code>
     * @return The spanId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getSpanId() {
      return instance.getSpanId();
    }
    /**
     * <pre>
     * A unique identifier for a span within a trace, assigned when the span
     * is created. The ID is an 8-byte array. An ID with all zeroes OR of length
     * other than 8 bytes is considered invalid (empty string in OTLP/JSON
     * is zero-length and thus is also invalid).
     * This field is optional. If the sender specifies a valid span_id then it SHOULD also
     * specify a valid trace_id.
     * The receivers SHOULD assume that the log record is not associated with a
     * span if any of the following is true:
     *   - the field is not present,
     *   - the field contains an invalid value.
     * </pre>
     *
     * <code>bytes span_id = 10;</code>
     * @param value The spanId to set.
     * @return This builder for chaining.
     */
    public Builder setSpanId(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSpanId(value);
      return this;
    }
    /**
     * <pre>
     * A unique identifier for a span within a trace, assigned when the span
     * is created. The ID is an 8-byte array. An ID with all zeroes OR of length
     * other than 8 bytes is considered invalid (empty string in OTLP/JSON
     * is zero-length and thus is also invalid).
     * This field is optional. If the sender specifies a valid span_id then it SHOULD also
     * specify a valid trace_id.
     * The receivers SHOULD assume that the log record is not associated with a
     * span if any of the following is true:
     *   - the field is not present,
     *   - the field contains an invalid value.
     * </pre>
     *
     * <code>bytes span_id = 10;</code>
     * @return This builder for chaining.
     */
    public Builder clearSpanId() {
      copyOnWrite();
      instance.clearSpanId();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:opentelemetry.proto.logs.v1.LogRecord)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "timeUnixNano_",
            "severityNumber_",
            "severityText_",
            "body_",
            "attributes_",
            co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue.class,
            "droppedAttributesCount_",
            "flags_",
            "traceId_",
            "spanId_",
            "observedTimeUnixNano_",
          };
          java.lang.String info =
              "\u0000\n\u0000\u0000\u0001\u000b\n\u0000\u0001\u0000\u0001\u0005\u0002\f\u0003\u0208" +
              "\u0005\t\u0006\u001b\u0007\u000b\b\u0006\t\n\n\n\u000b\u0005";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord> parser = PARSER;
        if (parser == null) {
          synchronized (co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord>(
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


  // @@protoc_insertion_point(class_scope:opentelemetry.proto.logs.v1.LogRecord)
  private static final co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord DEFAULT_INSTANCE;
  static {
    LogRecord defaultInstance = new LogRecord();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      LogRecord.class, defaultInstance);
  }

  public static co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<LogRecord> PARSER;

  public static com.google.protobuf.Parser<LogRecord> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
