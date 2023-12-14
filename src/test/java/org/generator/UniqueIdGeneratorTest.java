package org.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for simple App.
 */
public class UniqueIdGeneratorTest {

    @Test
    public void should_generate_unique_ids_even_in_parallel()
    {
        var generator = UniqueIdGenerator.create(Clock.systemUTC(), "abcdef");
        // generate 1000 ids in parallel to test uniqueness of each ids
        var ids = IntStream.range(0, 1000)
                .parallel()
                .mapToObj((_i) -> generator.generate()).collect(Collectors.toSet());
        // if unique ids are not generated, the set will not have 100 elements
        assertThat(ids.size()).isEqualTo(1000);
    }

    @Test
    public void should_generate_unique_id_in_time_order() throws InterruptedException {
        var uniqueIdGenerator = UniqueIdGenerator.create(Clock.systemUTC(), "abcdef");
        var id1 = uniqueIdGenerator.generate();
        var id2 = uniqueIdGenerator.generate();
        Thread.sleep(10);
        var id3 = uniqueIdGenerator.generate();
        assertThat(Arrays.asList(id1, id2, id3)).isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Test
    public void should_persist_unique_ids(@TempDir Path tempDir) {
        var fakeClock = new Clock() {
            private Instant instant = LocalDateTime.of(2024, 1, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);
            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return instant;
            }

            public void changeDay() {
                instant = instant.plusSeconds(24*3600);
            }
        };
        var persistentLog = new PersistentLog() {

            @Override
            public void persist(UniqueId uniqueId) {
                var timestamp = uniqueId.timestamp();
                var day = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC).format(ISO_LOCAL_DATE);
                try {
                    Files.writeString(
                            tempDir.resolve(day+".log"),
                            uniqueId + System.lineSeparator(),
                            CREATE, APPEND
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public Stream<String> read(String day) {
                var path = tempDir.resolve(tempDir.resolve(day+".log"));
                try {
                    return Files.lines(path, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        var uniqueIdGenerator = UniqueIdGenerator.create(fakeClock, "abcdef", persistentLog);
        var id1 = uniqueIdGenerator.generate();
        var id2 = uniqueIdGenerator.generate();
        fakeClock.changeDay();
        var id3 = uniqueIdGenerator.generate();
        assertThat(persistentLog.read("2024-01-01")).contains(id1.toString(), id2.toString());
        assertThat(persistentLog.read("2024-01-02")).contains(id3.toString());
    }
}
