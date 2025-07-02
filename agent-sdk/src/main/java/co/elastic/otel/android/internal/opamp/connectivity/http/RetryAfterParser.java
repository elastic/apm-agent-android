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
package co.elastic.otel.android.internal.opamp.connectivity.http;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import co.elastic.otel.android.internal.opamp.tools.SystemTime;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RetryAfterParser {
  private final SystemTime systemTime;
  private static final Pattern SECONDS_PATTERN = Pattern.compile("\\d+");
  private static final Pattern DATE_PATTERN =
      Pattern.compile(
          "[A-Za-z]{3}, [0-3][0-9] [A-Za-z]{3} [0-9]{4} [0-2][0-9]:[0-5][0-9]:[0-5][0-9] GMT");
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

  public static RetryAfterParser getInstance() {
    return new RetryAfterParser(SystemTime.getInstance());
  }

  RetryAfterParser(SystemTime systemTime) {
    this.systemTime = systemTime;
  }

  public Optional<Duration> tryParse(String value) {
    Duration duration = null;
    if (SECONDS_PATTERN.matcher(value).matches()) {
      duration = Duration.ofSeconds(Long.parseLong(value));
    } else if (DATE_PATTERN.matcher(value).matches()) {
      long difference = toMilliseconds(value) - systemTime.getCurrentTimeMillis();
      if (difference > 0) {
        duration = Duration.ofMillis(difference);
      }
    }
    return Optional.ofNullable(duration);
  }

  private static long toMilliseconds(String value) {
    return ZonedDateTime.parse(value, DATE_FORMAT).toInstant().toEpochMilli();
  }
}
