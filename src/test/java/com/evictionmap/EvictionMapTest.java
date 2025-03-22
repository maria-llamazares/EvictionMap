package com.evictionmap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class EvictionMapTest {

    private EvictionMap<String, String> testMap;

    @BeforeEach
    void testInit() {
        testMap = new EvictionMap<>(10, 1, 1);
    }

    @AfterEach
    void testClose() {
        testMap.close();
    }

    @Test
    void testTimeBasedEviction_KeyNotFound() {
        testMap.put("key1", "value1");
        assertEquals("value1", testMap.get("key1"));
        await().pollDelay(11, SECONDS).atMost(12, SECONDS).until(() -> true);
        assertNull(testMap.get("key1"));

    }

    @Test
    void testTimeBasedEviction_KeyHasExpired() {
        testMap = new EvictionMap<>(10, 1, 2);
        testMap.put("key1", "value1");
        assertEquals("value1", testMap.get("key1"));
        await().pollDelay(10, SECONDS).atMost(11, SECONDS).until(() -> true);
        assertNull(testMap.get("key1"));

    }

    @Test
    void testParametersConstructor_Exception() {
        assertThrows(IllegalArgumentException.class, () -> new EvictionMap<>(-1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EvictionMap<>(1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new EvictionMap<>(1, 1, -2));
    }

    @Test
    void testKeyNull() {
        assertThrows(NullPointerException.class, () -> testMap.put(null, "value1"));
        assertThrows(NullPointerException.class, () -> testMap.get(null));
    }

}
