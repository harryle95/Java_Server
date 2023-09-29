![Work Flow](https://github.com/harryle/Java_Server/actions/workflows/github-actions.yaml/badge.svg)

## How to run the program

## Design

### Lamport Clock Information

Lamport Timestamp is impregnated to the HTTP request and response message as
an [extension header](https://www.ietf.org/rfc/rfc2616.txt). HTTP Request and Response class can parse the HTTP
request/response to extract outgoing/incoming timestamp from a remote server. The format for LamportClock Timestamp is:

```yaml
Lamport-Timestamp: <int value>
```

### Synchronisation

- The servers implement a TCP-lite protocol as follows. The HTTP message contains a FIN bit header.
    - Upon startup, the client (`GETClient`/`ContentServer`) sends the `AggregationServer` a GET request with no
      URI ('/'),
      containing `Lamport-Timestamp: 0`.
    - The client then receives the response message containing the server Timestamp.
    - The client then send its actual request (GET/PUT).
    - The client then receives the response from the server, closing its connection.
    - The server then closes its connection.

#### Rationale

- When startup, the `GETClient` and `ContentServer` both have a timestamp of 0, hence there is no indication of a
  global order, only a partial order when their requests arrive at the `AggregationServer`. This means that even if the
  `GETClient` starts earlier than the `ContentServer`, it may be served the information from the `ContentServer` if the
  PUT request arrives earlier than the GET request.
- Doing a 3-way TCP handshake helps establish a global order. Since the `GETClient` or `ContentServer` startup times are
  now approximated by the timestamp of the SYNC ACK receipt.

### Exceptions:

#### Unhandled Exceptions:

The following errors will be raised to the user and not explicitly handled by the program.

- `UnknownHostException`: when the IP address of the `AggregationServer` cannot be resolved.
- `ConnectionException`: when there is no `AggregationServer` process listening on the port. Happens when
  the `AggregationServer`
  has not started up.
- `BindException`: when the port that the `AggregationServer` attempts to listen to is currently in used.
- `NoRouteToHostException`: when there is no internet or when the firewall blocks the message.
- `FileNotFoundError`: when content server file is not available.

#### Handled Exceptions:

The following errors are custom errors and will be handled.

- `RequestTimeOutException`: after a message is sent, if no response is received after a certain waiting period,
  the sending entity will resend the message. If no response is received after the 5th attempt,
  a `RequestTimeOutException` is raised.
- `ConnectionClosed`: unique to the `AggregationServer`. Happens when the messages are reordered and the client socket
  is closed
  while the server is still sending replies. The error will simply be logged.
- `MismatchedData`: when the result of the PUT request is different from its data. Unique to content server. Error is
  simply logged.
- `BackupNotFound`: when the system cannot find the backup files. Simply creates a new one if the current node is the
  master node, or request a backup file from system node.
- `DataNotFound`: when the data related to the station ID is not found on the server. 

### JSON Parsing:

Json data is parsed into mapping of `WeatherData` with weather station `id` as the key. Separate entries are
distinguished
from one another if a keyword is repeated. Entries without corresponding id field are invalid and will be removed.
Entries with the
same id are updated based on the recency of their timestamp `local_date_time_full`. If there are two entries with the
same id is present,
the one with the latest timestamp is parsed. If both entries have no timestamp, the one that appears first in the json
file will be parsed.

```yaml
# First entry starts here 
id: station1
press: 1390
local_date_time_full: 20230101160000
# Second entry starts here 
local_date_time_full: 20230101163000
press: 1400
id: station2
# Third entry - invalid no ID
local_date_time_full: 20230101173000
press: 1400
# Fourth entry - an old instance of station1 will not be parsed
id: station1
press: 1380
local_date_time_full: 20230101153000
```

### GET Client

- Client sends GET requests to aggregation server for resources. There can be two scenarios:
    - Stations ID is not provided; the aggregation server sends back a 200 OK response with an empty body:
      ```yaml
      GET / HTTP/1.1
      Host: <hostname>:<port>
      Accept: application/json
      Lamport-Timestamp: <value>
      ```
    - Station ID is provided; the aggregation server is to send the station data:
        ```yaml
      GET /<stationID> HTTP/1.1
      Host: <hostname>:<port>
      Accept: application/json
      Lamport-Timestamp: <value>
      ```

### Content Server

- Reads JSON file from filesystem and sends the PUT request to the `AggregationServer`. The PUT message is:
  ```yaml
  PUT /<fileName> HTTP/1.1
  Host: <hostname>:<port>
  Accept: application/json
  Content-Type: application/json
  Content-Length: <body.length()>
  Lamport-Timestamp: <value>
  
  <json string body>
  ```

### AggregationServer 



