package co.elastic.otel.android.compilation.tools.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.elastic.otel.android.compilation.tools.data.License;

public class LicensesProvider {

    public static Map<String, String> findLicensesMap() {
        Map<String, String> ids = new HashMap<>();
        InputStream resourceStream = LicensesIdsMatcher.class.getResourceAsStream("/licenses_ids.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(resourceStream)));

        try {
            while (reader.ready()) {
                String[] parts = reader.readLine().split("\\|");
                String id = parts[0];
                String name = parts[1];
                if (ids.containsKey(id)) {
                    throw new RuntimeException("Duplicated licence id: " + id);
                }
                ids.put(id, name);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ids;
    }

    public static List<License> findLicenses() {
        Map<String, String> licensesMap = findLicensesMap();
        List<License> licenses = new ArrayList<>();

        for (String id : licensesMap.keySet()) {
            licenses.add(new License(id, licensesMap.get(id)));
        }

        return licenses;
    }
}
