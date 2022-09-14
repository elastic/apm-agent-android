package co.elastic.apm.compile.tools.utils;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LicensesIdsMatcher {
    private static Map<String, String> ids;

    public static String findId(String licenseName) {
        Map<String, String> ids = getIds();
        String matchingId = null;
        String comparableName = curateLicenseName(licenseName);
        int shortestDistance = (int) Math.ceil(comparableName.length() / 2.0);
        LevenshteinDistance distanceFinder = LevenshteinDistance.getDefaultInstance();

        for (String id : ids.keySet()) {
            String predefinedName = ids.get(id);
            int distance = distanceFinder.apply(comparableName, predefinedName);
            if (distance > shortestDistance) {
                continue;
            }
            if (distance == shortestDistance && matchingId != null) {
                throw new RuntimeException("Ambiguous license id match for: '" + licenseName + "' -> matching ids: (" + matchingId + ", " + id + ")");
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
                String name = parts[1];
                if (ids.containsKey(id)) {
                    throw new RuntimeException("Duplicated licence id: " + id);
                }
                ids.put(id, curateLicenseName(name));
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ids;
    }

    /**
     * Makes all lowercase trims spaces and removes common parts of most licenses names that add noise to the comparison.
     */
    private static String curateLicenseName(String licenseName) {
        String curated = licenseName.toLowerCase(Locale.US);
        return curated.replaceAll("version|license|software|the|,", "")
                .replaceAll("[\\s\\t\\n]+", "");
    }
}
