package co.elastic.apm.compile.tools.utils;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LicensesIdsMatcher {
    private static Map<String, String> ids;

    public static String findId(String licenseName) {
        Map<String, String> ids = getIds();
        String matchingId = null;
        int shortestDistance = 500;
        LevenshteinDistance distanceFinder = LevenshteinDistance.getDefaultInstance();

        for (String id : ids.keySet()) {
            String value = ids.get(id);
            int distance = distanceFinder.apply(licenseName, value);
            if (distance > shortestDistance) {
                continue;
            }
            if (distance == shortestDistance) {
                throw new RuntimeException("Ambiguous license ids for: " + licenseName + " (" + matchingId + ", " + id + ")");
            }
            if (distance == 0) {
                return id;
            }
            shortestDistance = distance;
            matchingId = id;
        }

        return matchingId;
    }

    private static Map<String, String> getIds() {
        if (ids == null) {
            ids = findLicensesIds();
        }

        return ids;
    }

    private static Map<String, String> findLicensesIds() {
        Map<String, String> ids = new HashMap<>();
        InputStream resourceStream = LicensesIdsMatcher.class.getResourceAsStream("/licenses_ids.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(resourceStream)));

        try {
            while (reader.ready()) {
                String[] parts = reader.readLine().split("\\|");
                String id = parts[0];
                String description = parts[1];
                if (ids.containsKey(id)) {
                    throw new RuntimeException("Duplicated licence id: " + id);
                }
                ids.put(id, description);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ids;
    }
}
