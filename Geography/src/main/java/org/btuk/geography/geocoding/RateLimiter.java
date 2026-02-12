package org.btuk.geography.geocoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

// Tracks daily API usage
public final class RateLimiter {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final int GEOAPIFY_DAILY_LIMIT = 3000; // Free tier limit
    private static final long NOMINATIM_REQUEST_INTERVAL_MS = 1000; // 1 second for Nominatim

    private long nominatimLastRequest = 0;

    private static final boolean USE_GEOAPIFY_API = true;
    private static final String LIMITS_FILE = "daily_limits.json";

    private String date; // YYYY-MM-DD in UTC
    private int geoapifyCount;

    public RateLimiter() {
        this.date = LocalDate.now(ZoneId.of("UTC")).toString();
        this.geoapifyCount = 0;
        load();
    }

    synchronized boolean canUseGeoapify() {
        updateDate();
        return geoapifyCount < GEOAPIFY_DAILY_LIMIT;
    }

    synchronized boolean canUseNominatim() {
        return nominatimLastRequest + NOMINATIM_REQUEST_INTERVAL_MS < System.currentTimeMillis();
    }

    synchronized void setNominatimLastRequest(long nominatimLastRequest) {
        this.nominatimLastRequest = nominatimLastRequest;
    }

    synchronized long getDelayForNextNominatimRequest() {
        return nominatimLastRequest + NOMINATIM_REQUEST_INTERVAL_MS - System.currentTimeMillis();
    }

    synchronized void incrementGeoapify() {
        updateDate();
        geoapifyCount++;
        save();
    }

    private void updateDate() {
        String currentDate = LocalDate.now(ZoneId.of("UTC")).toString();
        if (!currentDate.equals(date)) {
            date = currentDate;
            geoapifyCount = 0;
        }
    }

    private void save() {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("date", date);
            node.put("geoapifyCount", geoapifyCount);
            objectMapper.writeValue(new File(LIMITS_FILE), node);
        } catch (IOException e) {
            System.err.println("Error saving daily limits: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(LIMITS_FILE);
        if (file.exists()) {
            try {
                JsonNode node = objectMapper.readTree(file);
                date = node.get("date").asText();
                geoapifyCount = node.get("geoapifyCount").asInt();
            } catch (IOException e) {
                System.err.println("Error loading daily limits: " + e.getMessage());
            }
        }
    }
}
