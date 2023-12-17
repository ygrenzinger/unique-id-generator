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

To maximise write performance, the best way is to use a append only strategy on a durable file storage (with some replication). 

With the current need described, the easiest solution could be object storage like S3 because it's very simple, durable and should meet the write performance requirements. There is Open Source solutions like [Ceph](https://ceph.io/en/) for On Promise. The file would change every days. It's basically what I am doing in my abstract datastore.


Others solutions:
- A streaming platform like Kafka. We could then use consumers to read the topic and, for example, consolidate IDs by day.
- A write heavy NoSQL database like Cassandra or ScyllaDB could also maybe a solution but I am less knowledgeable for the querying part and read the data "by day"  
