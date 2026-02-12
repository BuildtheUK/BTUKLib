package org.btuk.geography.geocoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.btuk.geography.Coordinate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReverseGeocoder {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f";
    private static final String GEOAPIFY_API_URL = "https://api.geoapify.com/v1/geocode/reverse?lat=%f&lon=%f&apiKey=%s";
    private static final String GEOAPIFY_API_KEY = "YOUR_GEOAPIFY_API_KEY_HERE"; // Replace with your key
    private static final String CACHE_FILE = "cache.json";

    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    private static final RateLimiter RATE_LIMITER = new RateLimiter();

    private static final ExecutorService nominatimExecutorService = Executors.newSingleThreadExecutor();
    private static final ExecutorService geoapifyExecutorService = Executors.newSingleThreadExecutor();

    // Load cache at startup
    static {
        loadCache();
    }

    private static void saveCache() {
        try {
            ObjectNode cacheNode = objectMapper.createObjectNode();
            cache.forEach(cacheNode::put);
            objectMapper.writeValue(new File(CACHE_FILE), cacheNode);
        } catch (IOException e) {
            System.err.println("Error saving cache: " + e.getMessage());
        }
    }

    private static void loadCache() {
        File file = new File(CACHE_FILE);
        if (file.exists()) {
            try {
                JsonNode cacheNode = objectMapper.readTree(file);
                cacheNode.fields().forEachRemaining(entry ->
                        cache.put(entry.getKey(), entry.getValue().asText()));
            } catch (IOException e) {
                System.err.println("Error loading cache: " + e.getMessage());
            }
        }
    }

    private static String getCountryNominatim(Coordinate coordinate) {
        try {
            RATE_LIMITER.setNominatimLastRequest(System.currentTimeMillis());
            String urlString = String.format(Locale.US, NOMINATIM_API_URL, coordinate.latitude(), coordinate.longitude());
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "JavaReverseGeocoder/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                if (responseCode == 429) {
                    System.err.println("Nominatim rate limit hit, retrying after delay...");
                    Thread.sleep(RATE_LIMITER.getDelayForNextNominatimRequest());
                    return getCountryNominatim(coordinate);
                }
                System.err.println("Nominatim HTTP error: " + responseCode);
                return "Unknown";
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            JsonNode jsonNode = objectMapper.readTree(response.toString());
            JsonNode addressNode = jsonNode.path("address");
            if (!addressNode.isMissingNode()) {
                String countryCode = addressNode.path("country_code").asText(null);
                if (countryCode != null && countryCode.matches("[a-zA-Z]{2}")) {
                    return countryCode.toUpperCase();
                }
                System.err.println("Invalid Nominatim country_code for lat=" + coordinate.latitude() + ", lon=" + coordinate.longitude() + ": " + countryCode);
            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                System.err.println("Nominatim IO error, retrying: " + e.getMessage());
                try {
                    Thread.sleep(RATE_LIMITER.getDelayForNextNominatimRequest());
                } catch (InterruptedException ex) {
                    // Ignored.
                }
                return getCountryNominatim(coordinate);
            }
            System.err.println("Nominatim error for lat=" + coordinate.latitude() + ", lon=" + coordinate.longitude() + ": " + e.getMessage());
        }
        return "Unknown";
    }

    private static String getCountryGeoapify(Coordinate coordinate) {
        if (!RATE_LIMITER.canUseGeoapify()) {
            System.err.println("Geoapify daily limit reached, use nominatim.");
            try {
                // We'll use the other thread executor and wait,
                // since we've reached the limit this thread won't be used until the next day.
                return nominatimExecutorService.submit(() -> getCountryNominatim(coordinate)).get();
            } catch (InterruptedException | ExecutionException e) {
                // Ignored.
                return "Unknown";
            }
        }
        try {
            String urlString = String.format(Locale.US, GEOAPIFY_API_URL, coordinate.latitude(), coordinate.longitude(), GEOAPIFY_API_KEY);
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Geoapify HTTP error: " + responseCode);
                return nominatimExecutorService.submit(() -> getCountryNominatim(coordinate)).get();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            JsonNode jsonNode = objectMapper.readTree(response.toString());
            JsonNode featuresNode = jsonNode.path("features");
            if (featuresNode.isArray() && !featuresNode.isEmpty()) {
                JsonNode propertiesNode = featuresNode.get(0).path("properties");
                String countryCode = propertiesNode.path("country_code").asText(null);
                if (countryCode != null && countryCode.matches("[a-zA-Z]{2}")) {
                    String alpha2Code = countryCode.toUpperCase();
                    RATE_LIMITER.incrementGeoapify();
                    return alpha2Code;
                }
                System.err.println("Invalid Geoapify country_code for lat=" + coordinate.latitude() + ", lon=" + coordinate.longitude() + ": " + countryCode);
            }
        } catch (Exception e) {
            System.err.println("Geoapify error for lat=" + coordinate.latitude() + ", lon=" + coordinate.longitude() + ": " + e.getMessage());
        }
        return "Unknown";
    }

    private static String getCountryFromCache(Coordinate coordinate) {
        String cacheKey = coordinate.getCacheKey();
        return cache.get(cacheKey);
    }

    private static Future<String> getCountrySingle(Coordinate coord) {
        if (RATE_LIMITER.canUseNominatim()) {
            return nominatimExecutorService.submit(() -> getCountryNominatim(coord));
        } else if (RATE_LIMITER.canUseGeoapify()) {
            return geoapifyExecutorService.submit(() -> getCountryGeoapify(coord));
        } else {
            // Fallback is always nominatim.
            return nominatimExecutorService.submit(() -> getCountryNominatim(coord));
        }
    }

    public static Map<Coordinate, String> getCountries(List<Coordinate> coordinates) {
        Map<Coordinate, String> countries = new HashMap<>();
        Map<Coordinate, Future<String>> futures = new HashMap<>();

        for (Coordinate coord : coordinates) {
            String cachedCountry = getCountryFromCache(coord);
            if (cachedCountry != null) {
                countries.put(coord, cachedCountry);
            } else {
                futures.put(coord, getCountrySingle(coord));
            }
        }

        // Wait for all the future to complete.
        for (Map.Entry<Coordinate, Future<String>> future : futures.entrySet()) {
            try {
                String country = future.getValue().get();
                cache.put(future.getKey().getCacheKey(), country);
                saveCache();
                countries.put(future.getKey(), country);
            } catch (InterruptedException | ExecutionException e) {
                countries.put(future.getKey(), "Unknown");
                // throw new RuntimeException(e);
            }
        }
        return countries;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        List<Coordinate> coordinates = Arrays.asList(
                new Coordinate(48.8566, 2.3522),   // Paris, France -> FR
                new Coordinate(51.5074, -0.1278),  // London, United Kingdom -> GB
                new Coordinate(40.7128, -74.0060), // New York, United States -> US
                new Coordinate(35.6762, 139.6503), // Tokyo, Japan -> JP
                new Coordinate(-33.8688, 151.2093), // Sydney, Australia -> AU
                new Coordinate(18.4663, -66.1057), // San Juan, Puerto Rico -> PR
                new Coordinate(64.1333, -21.9333), // Reykjavik, Iceland -> IS
                new Coordinate(-54.8019, -68.3030), // Ushuaia, Argentina -> AR
                new Coordinate(0.0, 0.0),          // Atlantic Ocean -> ""
                new Coordinate(22.3193, 114.1694), // Hong Kong -> HK
                new Coordinate(42.7339, 25.4858)   // Bulgaria -> BG
        );

        Map<Coordinate, String> countries = getCountries(coordinates);
        for (Map.Entry<Coordinate, String> country : countries.entrySet()) {
            System.out.printf(Locale.US, "Lat: %.6f, Lon: %.6f -> Country: %s%n",
                    country.getKey().latitude(), country.getKey().longitude(), country.getValue());
        }
        nominatimExecutorService.shutdown();
        geoapifyExecutorService.shutdown();
        System.out.printf("Completed in %dms", System.currentTimeMillis() - start);
    }
}
