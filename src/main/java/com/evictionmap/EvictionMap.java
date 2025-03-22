package com.evictionmap;

import java.util.Map;
import java.util.concurrent.*;

public class EvictionMap<K, V> {

    private final Map<K, V> dataMap = new ConcurrentHashMap<>();                // Store key-value pairs
    private final Map<K, Long> timeMap = new ConcurrentHashMap<>();             // Store when each key will expire
    private final long durationMillis;                                          // How long entries should live (in milliseconds)
    private final ScheduledExecutorService scheduler;                           // Background task scheduler

    /**
     * Creates a new EvictionMap that removes entries after a set time.
     *
     * @param durationSeconds how long (in seconds) entries stay in the map before removal
     * @param initialDelay time (in seconds) to wait before first cleanup
     * @param period how often (in seconds) cleanup runs after the first one
     * @throws IllegalArgumentException if durationSeconds, initialDelay, or period is zero or negative
     */
    public EvictionMap(long durationSeconds, int initialDelay, int period) {

        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0.");
        }

        if (initialDelay <= 0) {
            throw new IllegalArgumentException("Initial delay must be greater than 0.");
        }

        if (period <= 0) {
            throw new IllegalArgumentException("Period must be greater than 0.");
        }

        this.durationMillis = durationSeconds * 1000;                           // Convert to milliseconds
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        initCleanupProcess(initialDelay, period);
    }

    /**
     * Starts the background thread that will remove old entries.<br>
     * This runs every second to check if any entries have expired.
     */
    private void initCleanupProcess(int initialDelay, int period) {
        scheduler.scheduleAtFixedRate(this::removeOldEntries, initialDelay, period, TimeUnit.SECONDS);
    }

    /**
     * Deletes old entries from the maps.<br>
     *
     * Checks all keys in timeMap and removes any that have expired.<br>
     * When we find an expired key:<br>
     * - Remove it from the dataMap<br>
     * - Remove it from the timeMap<br>
     *
     * This runs automatically every second.
     */
    private void removeOldEntries() {

        long currentTime = System.currentTimeMillis();

        timeMap.entrySet().removeIf(entry -> {
            K key = entry.getKey();
            Long expiryTime = entry.getValue();

            if (currentTime > expiryTime) {
                dataMap.remove(key);
                return true;
            }
            return false;
        });
    }

    /**
     * Stores a key-value pair in the map.<br>
     * If the key already exists, the old value is replaced.
     *
     * @param key   the key to store
     * @param value the value to store
     * @throws NullPointerException if the key is null
     */
    public void put(K key, V value) {

        if (key == null) {
            throw new NullPointerException("Key cannot be null.");
        }

        long expiryTime = System.currentTimeMillis() + durationMillis; // Expiry moment
        dataMap.put(key, value);
        timeMap.put(key, expiryTime);
    }

    /**
     * Gets the value for a key, if it exists and hasn't expired.
     *
     * @param key the key to look up
     * @return the value, or null if the key doesn't exist or has expired
     */
    public V get(K key) {

        if (key == null) {
            throw new NullPointerException("Key cannot be null.");
        }

        // Check if the key exists in map
        Long expiryTime = timeMap.get(key);

        if (expiryTime == null) {
            // Key not found
            return null;
        }

        // Check if the key has expired
        if (System.currentTimeMillis() > expiryTime) {
            // Remove expired entry
            dataMap.remove(key);
            timeMap.remove(key);
            return null;
        }

        // Key exists and is valid
        return dataMap.get(key);
    }

    /**
     * Closes this map and releases resources.<br>
     * Should be called when the map is no longer needed.
     */
    public void close() {
        // Stop the cleanup thread
        scheduler.shutdownNow();

        // Clear all data
        dataMap.clear();
        timeMap.clear();
    }

}