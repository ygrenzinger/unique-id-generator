package org.generator;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Random;

/**
 * Unique ID Generator
 */
public class UniqueIdGenerator {
    private static final long MAX_SEQUENCE = Short.MAX_VALUE - 1;

    private final PersistentLog persistentLog;

    private final Clock clock;
    private final byte[] nodeId;
    private volatile long lastTimestamp = -1L;
    private short sequence = 0;


    public static UniqueIdGenerator create() {
        byte[] nodeId = new byte[3];
        new Random().nextBytes(nodeId);
        return new UniqueIdGenerator(Clock.systemUTC(), nodeId, uniqueId -> {});
    }

    public static UniqueIdGenerator create(Clock clock, String nodeId) {
        return new UniqueIdGenerator(clock, nodeId.getBytes(StandardCharsets.UTF_8), uniqueId -> {});
    }

    public static UniqueIdGenerator create(Clock clock, String nodeId, PersistentLog persistentLog) {
        return new UniqueIdGenerator(clock, nodeId.getBytes(StandardCharsets.UTF_8), persistentLog);
    }

    public UniqueIdGenerator(Clock clock, byte[] nodeId, PersistentLog persistentLog) {
        this.clock = clock;
        this.nodeId = nodeId;
        this.persistentLog = persistentLog;
    }

    /**
     * Generates a unique id
     *
     * @return unique id record
     */
    public synchronized UniqueId generate() {
        var currentTimestamp = this.clock.instant().toEpochMilli();

        if(currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence++;
            if(sequence == MAX_SEQUENCE) {
                // Choosing to let the client handle this scenario, for example with a retry mechanism
                throw new IllegalStateException("Sequence Exhausted!");
            }
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0;
            lastTimestamp = currentTimestamp;
        }
        var uniqueID = UniqueId.from(currentTimestamp, sequence, nodeId);
        persistentLog.persist(uniqueID);
        return uniqueID;
    }
}
