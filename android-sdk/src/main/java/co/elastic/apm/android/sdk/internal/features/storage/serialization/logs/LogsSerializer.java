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
package co.elastic.apm.android.sdk.internal.features.storage.serialization.logs;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.models.LogCollection;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Mapper;
import io.opentelemetry.proto.logs.v1.LogsData;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public final class LogsSerializer {

    public static byte[] serialize(List<LogRecordData> logs) {
        LogsData protoLogs = Mapper.get().map(new LogCollection(logs));

        return protoLogs.toByteArray();
    }

    public static List<LogRecordData> deserialize(byte[] serialized) {
        try {
            LogsData logsData = LogsData.parseFrom(serialized);
            LogCollection collection = Mapper.get().map(logsData);
            return collection.logs;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
