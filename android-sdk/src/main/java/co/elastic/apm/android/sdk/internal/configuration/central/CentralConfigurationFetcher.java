package co.elastic.apm.android.sdk.internal.configuration.central;

import android.net.Uri;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class CentralConfigurationFetcher {
    private final String endpoint;
    private final String serviceName;
    private final String serviceEnvironment;
    private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
    private final byte[] buffer = new byte[4096];

    public CentralConfigurationFetcher(String endpoint,
                                       String serviceName,
                                       String serviceEnvironment) {
        this.endpoint = endpoint;
        this.serviceName = serviceName;
        this.serviceEnvironment = serviceEnvironment;
    }

    public Map<String, String> fetch() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) getUrl().openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        try {
            final JsonReader<Object> reader = dslJson.newReader(connection.getInputStream(), buffer);
            reader.startObject();
            return MapConverter.deserialize(reader);
        } finally {
            connection.disconnect();
        }
    }

    private URL getUrl() throws MalformedURLException {
        Uri uri = Uri.parse(endpoint).buildUpon()
                .appendQueryParameter("service.name", serviceName)
                .appendQueryParameter("service.environment", serviceEnvironment)
                .build();
        return new URL(uri.toString());
    }
}
