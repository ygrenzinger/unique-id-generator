package org.generator;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class UniqueIdTest {
    final Long timestamp = LocalDateTime.of(2024, 1, 1, 0, 0, 0).toInstant(java.time.ZoneOffset.UTC).toEpochMilli();

    @Test
    public void should_create_unique_id()
    {
        var uniqueId = UniqueId.from(timestamp, (short) 1, "abcdef".getBytes(StandardCharsets.UTF_8));
        assertThat(uniqueId.timestamp()).isEqualTo(timestamp);
        assertThat(uniqueId.sequence()).isEqualTo((short) 1);
        assertThat(uniqueId.nodeId()).isEqualTo("abcdef".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void should_convert_to_string_and_from_string()
    {
        var uniqueId = UniqueId.from(timestamp, (short) 1, "abcdef".getBytes(StandardCharsets.UTF_8));
        var stringRepresentation = "0000018c-c251-f400-0001-616263646566";
        assertThat(uniqueId.toString()).isEqualTo(stringRepresentation);
        assertThat(UniqueId.fromString(stringRepresentation)).isEqualTo(uniqueId);
    }

    @Test
    public void should_hashcode() {
        var uniqueId1 = UniqueId.from(timestamp, (short) 1, "abcdef".getBytes(StandardCharsets.UTF_8));
        var uniqueId2 = UniqueId.from(timestamp, (short) 1, "abcdef".getBytes(StandardCharsets.UTF_8));
        assertThat(uniqueId1.hashCode()).isEqualTo(uniqueId2.hashCode());
    }

    @Test
    public void should_compare_unique_ids()
    {
        var uniqueId1 = UniqueId.from(timestamp, (short) 1, "abcdef".getBytes(StandardCharsets.UTF_8));
        var uniqueId2 = UniqueId.from(timestamp, (short) 2, "abcdef".getBytes(StandardCharsets.UTF_8));
        var uniqueId3 = UniqueId.from(timestamp, (short) 1, "abcdef".getBytes(StandardCharsets.UTF_8));
        var uniqueId4 = UniqueId.from(timestamp, (short) 0, "abcdef".getBytes(StandardCharsets.UTF_8));
        assertThat(uniqueId1).isLessThan(uniqueId2);
        assertThat(uniqueId1).isEqualTo(uniqueId3);
        assertThat(uniqueId1).isGreaterThan(uniqueId4);
    }

}
