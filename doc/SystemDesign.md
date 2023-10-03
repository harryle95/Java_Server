# System Design

## Lamport Clock

All communicating entities (GETClient, ContentServer, AggregationServer, LoadBalancer) implements the LamportClock, which
can be found in `src/src/utility/LamportClock`. The LamportClock object stores a timestamp value that is incremented everytime
a socket send and receive method is performed. The LamportClock object's methods that increment the timestamps - i.e
`advanceAndGetTimeStamp` and `advanceAndSetTimeStamp` are synchronized to mark that the code to increase timestamp is a critical section.

## HTTPRequest and HTTPResponse

The HTTP Request and Response classes are implemented in `src/src/utility/http`. The implementation uses the Builder 
Design Pattern to build a request by adding the right components - i.e. request method, http version, header fields,
body and so on. 

## Message Exchange

The HTTP Messages are exchanged over sockets. To simplify the send and receive process, the messages are first encoded 
using UTF8 on the sender side and decoded on the receiver. This however, makes the system incapable of exchanging messages
with standard HTTP entities. This is a short coming, but is not part of the assignment so I have not provided a better solution.


### GETClient Workflow:

- GETClient connects to `AggregationServer` using provided hostname and port and station ID. The `argv` arguments are:
  - `hostname:port [stationID]`
  - `http://hostname:port [stationID]`
  - `http://hostname[.domain]*:port [stationID]`
- Upon receiving the response to the GET request, the GETClient then shutdowns.
- The GETClient is to retry if no response is received after 5 seconds. If 5 retry attempts are reached, an error is thrown.

### ContentServer Workflow:

- ContentServer connects to `AggregationServer` using provided hostname, port, and a fileName, specifying the path to a local
file containing weather data. The `argv` arguments are: 
  - `hostname:port fileName`
  - `http://hostname:port fileName`
  - `http://hostname[.domain]*:port fileName`
- ContentServer first sends an empty GET request to receive the server Lamport Clock timestamp contained in the response message. This 
timestamp sets the current timestamp of the ContentServer.
- ContentServer then sends a PUT request containing the data read from fileName.
- Upon receiving the response for the PUT request, the ContentServer then shutdowns.
- The ContentServer is to retry if no response is received after 5 seconds. If 5 retry attempts are reached, an error is thrown.

### Aggregation Workflow 

#### Archive and Database

Are the two different files stored in memory by AggregationServer. 

- Archive stores Content Server information, and is a `ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>>` object.

```yaml
serverName:
  fileName:
    Value: LastPUTRequestContent
    Timestamp: TimeWhenPutIsReceived
```
- Database stores stationID information, and is a `ConcurrentMap<String, String>` object:

```yaml
stationID: stationIDContent
```

#### Handling GET Request:

The uri is extracted from the HTTP GET message:

- If the uri is null, returns a 204 - No Content. 
- If the uri is provided and the station id is in the system, returns a 200 OK
- If the uri is provided and the station id is not in the system, returns a 404 Not Found

#### Handling PUT Request:

The uri is extracted from the HTTP Put message:

- If the ContentServer id is in `archive`, returns a 200 OK response with the PUT content.
- If the ContentServer is not in `archive`, returns a 201 Created with the PUT content in the body.
- If PUT content, together with the timestamp are put to `archive`.
- The PUT content, station data separated by `id` fields is extracted and used to populate/update the `database`.

#### Connection Handling Task Queue and Schedule Tasks

The Aggregation Server is multi-threaded: it can handle multiple concurrent GET and PUT requests from different 
GET clients and PUT clients. The server has an `ExecutorService` `connectionHandlerPool` for connection send/receive tasks. Whenever the 
serverSocket detections an incoming request, the request is directed to `connectionHandlerPool` to open a connection, receive the HTTPRequest.
The HTTPRequest is then directed to `requestHandlerPool` which is a thread pool with an underlying priority queue that queues tasks by their Lamport timestamp. 
To ensure strict consistency, the `requestHandlerPool` contains one single execution thread. Once the request handler finishes with its task, 
the result is delivered back to the requesting thread in `connectionHandlerPool`, which is then used to send back to the client. 

If the request is a PUT, its metadata information is also put in an update queue. This update queue keeps track of the PUT update sequence. If the 
queue exceeds 20, the first items are pop and removed from `archive`. This fulfills the requirements that out-of-date files are removed from the system.

When a connection with a ContentServer is terminated, a scheduled task is submitted to the `schedulePool`, which cleans up the corresponding entry in `archive` after 30s. This 
fulfills the requirements that files are deleted after 30s of inactivity from the server. 

The server also runs scheduled backup task, which save to local a copy of `archive` and `database`. This job is run every 15 minutes. 

#### Persistency:

The backups are stored at `src/resources/FileSystem` as `archive.backup` and `database.backup` which are serialised files from
`database` and `archive`. When the server starts up, it tries to restore from backup, if the backup files exists, otherwise it starts
from scratch. 

#### Fault-Tolerance: Load Balancer

Instead of using the AggregationServer, the LoadBalancer should be used to provide high-availability. The load balancer 
has a list of Aggregation Server it is connected to. It selects the first available server as a leader which handles all PUT and GET requests.
The leader's health is checked every 30s. If the leader does not respond, another leader is elected from the Aggregation Server pool. 


### Expected Behaviours:

The clarifications here are needed because the assignment brief does not cover the following behaviour:

- When the data from a Content Server is removed, it means the data stored in `archive` is removed, but not the data in `database`: if the old entry contains station id `5000`, after the data is purged, query for id `5000` is still sucessful. 
- The content server is stateless. It implements a 2 step protocol which first sends a GET to get timestamp, which sets its Lamport Clock, then sends a PUT. No file is saved for Content Server. 
- If the server is down, the client (GETClient/PUTClient) enters a retry loop and throws an error if timeout. When a server fails, all requests it currently handle are removed. The clients will need to resend its request to the server. 

### Json Parsing

Text files and Json files with key: value pair can be parsed using the program in `src/src/utils/weatherJSON`. The program
cannot handle nested key value pairs. 