# EvictionMap

A generic map with time-based eviction policy that allows storing key-value pairs for a specific duration.

## Description

`EvictionMap<K,V>` is a data structure that acts as a key-value map with an automatic time-based eviction policy: entries are automatically removed after the specified duration has passed since the entry was created or last updated.

## Main Features

- Generic data structure that allows any type of key and value
- Automatic time-based eviction policy
- Thread-safe implementation
- Efficient use of system resources

## Implementation

### Structure

The implementation uses:

- A `ConcurrentHashMap` to store key-value pairs
- A second `ConcurrentHashMap` to track expiration times
- A `ScheduledExecutorService` to periodically run the cleanup task

### Main Methods

- `public void put(K key, V value)`: Stores a key-value pair in the map. If the key already exists, replaces the old value.
- `public V get(K key)`: Returns the value associated with the key in the map or null if no valid mapping exists.
- `public void close()`: Closes the map and releases resources (should be called when the map is no longer needed).

### Implementation Details

#### Eviction Mechanism

The class implements a cleanup process that:

1. Runs periodically in the background
2. Checks all entries to identify those that have expired
3. Removes expired entries from both maps (data and times)

#### Thread Safety

The implementation is thread-safe thanks to:

- Use of `ConcurrentHashMap` for the underlying data structures
- A single cleanup thread that handles data eviction
- Atomic operations for checking and removing expired entries

## Design Decisions

1. **Data Structures**: I chose `ConcurrentHashMap` for its superior performance in concurrent environments and its ability to handle multiple simultaneous reads and writes without excessive locking.

2. **Separation of Responsibilities**: Keeping the data and expiration times in separate maps provides a clear separation of responsibilities and simplifies implementation.

3. **Real-time Verification**: The `get()` method checks for expiration at query time, ensuring that expired data is not returned even if the scheduled cleanup task has not yet removed it.

4. **Periodic Cleanup**: The implementation includes a periodic cleanup process to prevent memory leaks, ensuring that expired entries are removed even if they are not accessed.

5. **Resource Management**: The class provides a `close()` method to properly release resources and stop the cleanup thread.

## Considerations for Future Development

This project has been my first experience with concurrent programming and thread management. For use cases with a large number of entries (more than 100k) or long duration periods (in hours or days), I researched some possible improvements that could be explored in the future:

1. **Parameter Adjustments**: The current implementation allows configuring both the duration of entries and the cleanup frequency. For large volumes of data, it might be beneficial to use a lower cleanup frequency (e.g., every minute instead of every second).

2. **Size Limitation**: In addition to time-based eviction, a maximum size limit for the map could be implemented to prevent memory issues.

3. **Performance Testing Improvements**: It would be interesting to develop specific tests to measure performance with different loads and configurations.

As I gain more experience with concurrent programming, I will explore more advanced techniques to optimize this data structure.

## Requirements

- Java 8 or higher
- JUnit 5 for running tests
- Maven for dependency management

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── evictionmap/
│               └── EvictionMap.java
└── test/
    └── java/
        └── com/
            └── evictionmap/
                └── EvictionMapTest.java
```

## How to Use

```java
// Create a map that evicts entries after 10 seconds
EvictionMap<String, String> userMap = new EvictionMap<>(10, 1, 1);

// Store a value
userMap.put("user1", "Harry Quebert");

// Retrieve the value (before expiration)
String userName = userMap.get("user1"); // Returns "Harry Quebert"
System.out.println(userName); // Prints: Harry Quebert

// Wait for entry to expire
Thread.sleep(11000);

// Try to retrieve after expiration
String expiredName = userMap.get("user1"); // Returns null
System.out.println(expiredName); // Prints: null

// Always close when finished
userMap.close();
```

## Getting Started

### Prerequisites

- Java 8 JDK or higher
- Maven 3.9.9 or higher
- Git (optional, for cloning the repository)
- IDE: IntelliJ IDEA (recommended) or any other Java IDE with Maven support

### Installation

1. Clone the repository (or download the ZIP file):

   ```bash
   git clone https://github.com/maria-llamazares/EvictionMap.git
   cd EvictionMap
   ```

2. Build the project:

   ```bash
   mvn clean install
   ```

This command will:
- Clean any previous builds
- Compile the source code
- Run the unit tests
- Package the project
- Install the package to your local Maven repository

### Using as a Dependency

After installing, you can use EvictionMap in another project by adding this dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.evictionmap</groupId>
    <artifactId>eviction-map</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Import into IDE

#### IntelliJ IDEA

1. Go to File > Open...
2. Navigate to and select the project directory
3. Click OK