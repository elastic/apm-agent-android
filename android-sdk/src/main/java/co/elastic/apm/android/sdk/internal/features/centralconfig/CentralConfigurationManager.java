package co.elastic.apm.android.sdk.internal.features.centralconfig;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CentralConfigurationManager {
    private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
    private final byte[] buffer = new byte[4096];

    public void sync() throws IOException {
        final JsonReader<Object> reader = dslJson.newReader(getConfigurationInputStream(), buffer);
        reader.startObject();
        Map<String, String> map = MapConverter.deserialize(reader);
    }

    private InputStream getConfigurationInputStream() {
        return null;
    }
}
