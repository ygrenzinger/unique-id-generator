# Unique id generator

## Part 1
This Java project contains a simple class that generates unique ids in thread safe way.
It's based on Maven build system and JUnit for testing.
Because it's using Record, Java 17 is required.

**Design choices:**  

Unique ids are generated using a combination of timestamp, a counter and a node id.
The id is composed of 128 bits, 64 bits for the timestamp, 16 bits for the counter and 48 bits for the node id.
- The timestamp is the number of milliseconds since the epoch (01/01/1970).
- The counter is incremented each time an id is generated in the same millisecond.
- The node id is a unique identifier for the node generating the id. We usually represent it with a 6 chars string.

The byte manipulation is done using ByteBuffer. It's maybe not as efficient as using bit manipulation but it's more readable.

Even if the generator should be unique inside a node (JVM) using singletons or IoC, I decided to make it thread safe to be able to use it in a multi-threaded environment.

You can run tests using: `mvn test`
You can run the main class using: `mvn compile exec:java -Dexec.mainClass="App" -Dexec.args="node01"`


## Part 2

Generating unique ids is a write heavy operation.
If we want an auditable log, we need to store it in a centralised persistent storage.
A streaming platform like Kafka is a good candidate for this use case.
We can use a Kafka topic and use consumer to consolidate the ids by the end of day with a batch job and store aggregation by day in durable file storage like S3.

Other solutions could be :
- to use a database like Cassandra or ScyllaDB that are optimised for write heavy operations.
- to directly write into a durable file storage using the current day as file path (this close to the local file storage I use in the abstract store).
