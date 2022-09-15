package co.elastic.apm.compile.tools.utils;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

        Map<String, String> licenses = LicensesProvider.findLicensesMap();
        for (String id : licenses.keySet()) {
            ids.put(id, curateLicenseName(licenses.get(id)));
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
