package co.elastic.apm.compile.tools.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LicensesProvider {

    public static Map<String, String> findLicenses() {
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
}
