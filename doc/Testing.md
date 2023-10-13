# Testing

## Overview

All unit tests and integration tests are written using JUNIT test and stored under `src/test/`. The test folder mirrors
the content of the `src` directory. 

## Run tests:

```bash
make run_test
```

## Note

In order to test the behaviour of the servers, I have created auxiliary methods that speed up the behaviour of certain tasks.
For instance, to simulate that data is purged after 30s, I set the expunge period to 10 ms or so to speed up the process. This,
however might cause some tests to behave in correctly.

Additionally, when running the test, you might see some exceptions being thrown, but the test cases pass. The exceptions are caused 
by the scheduled Runnable being rejected by the ScheduledExecutorService when the latter is shuting down. It does not affect the 
status of the program. 

The table below shows some of the main integration tests. There are other 127 unit tests that are not listed here. 


## Integration test table

| TestClass                               | TestMethod                                                                | Scenario                                                                                                                                                                             |
|-----------------------------------------|---------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|            HTTPErrorMessageHandlingTest | testGETNotFoundIDGives404NotFound                                         | When Get request is made for a station ID that does not exist on server, the reply is 404 Not Found                                                                                  |
|                                         | testGETBlankGives204NoContent                                             | When Get request is GET / 1.1, where there is no uri,the reply is 204 No Content                                                                                                     |
|                                         | testGETFoundIDGives200OK                                                  | When GET request is for data in the database, response gives 200 OK  with the data in body                                                                                           |
|                                         | testWhenServerShutsDownClientThrowsException                              | When the server is shutdown when message is exchanged between the server  and client, Client throws an exception                                                                     |
|                                         | testClientErrorDoesNotShutDownServer                                      | When client encounters error, the server does not shutdown                                                                                                                           |
|                                         | testClientPrematureCloseDoesNotShutDownServer                             | When client closes before the client workflow is complete, the server does not get shutdown                                                                                          |
|                                         | testClientCloseDoesCloseSocket                                            | When client is closed, all resources are freed                                                                                                                                       |
|                                         | testServerCloseDoesCloseSocket                                            | When the server is closed, all resources are freed                                                                                                                                   |
|                                         | testServerHandleMultipleGETClientsSuccessfully                            | When there are requests from multiple concurrent clients, the server works                                                                                                           |
|                                         | testFirstPUTRequestGives201Created                                        | When a PUT request is made for the first time, the response is 201 Created                                                                                                           |
|                                         | testSubsequentPUTRequestGives200OK                                        | When a PUT request is made after the first PUT request, the response is 200 OK                                                                                                       |
|                                         | testPUTRequestBeingIdempotent                                             | When the same PUT is made several times for the same data, it stays the same                                                                                                         |
|                                         | testPOSTRequestReceives400BadRequest                                      | When other HTTP header request is made, a 400 Bad Request response is made                                                                                                           |
| MultipleSerialPUTTest                   | testPUTRequestsUpdateDatabase                                             | When a PUT request is made, the data is registered on the database                                                                                                                   |
|                                         | testInterleavedGETPUTRequests                                             | When multiple PUT, GET, PUT, GET requests are interleaved, the response match the state of the database                                                                              |
|                                         | testIndependentPUTDoNotInterfereOneAnotherSerial                          | When two PUT requests are independent of one another (different station IDs), they do not alter each other's results                                                                 |
|                                         | testIndependentPUTDoNotInterfereOneAnotherConcurrent                      | Same as previous, but the requests are not concurrent instead of serial                                                                                                              |
|                                         | testOldestEventsEjectedFromArchive                                        | When 21 PUT requests are made to the AggServer, the first PUT request is ejected after the last PUT request is processed due to being stale                                          |
|                                         | testFilesRemovedAfterConnectionClosed                                     | When a connection closes from a PUT request, 30 seconds later, the PUT data is removed from archive                                                                                  |
|                                         | testWhenUpdateQueueContainsTheSameEntryArchiveDoNotRemove                 | When mutliple updates for the same file is made, the file is not removed from archive. This is because archive already stores the latest version. The old version is already purged. |
|                                         | testWhenUpdateQueueContainsUpdatedVersionOfTheSameEntryArchiveDoNotRemove | Same as previous, but the same updates may be interleaved with other updates                                                                                                         |
|                                         | testAllFilesRemovedSteadStateAllFiles                                     | After all 20 PUT requests are made, after a while, the server will expunge all archive data                                                                                          |
|                                         | testPUTRequestAreSerialisedCorrectlyFourTasks                             | When multiple updates concerning the same station are made serially, the database store the latest version based on timestamp                                                        |
| MultiplePUTWithCompositeDataTest        | testCompositePUTUseLatestTimer                                            | When a file from Content Server contains data for the same station id, the latest, determined using full date time, is registered on the database                                    |
|                                         | testConsecutivePUTOnSameIDUseLatestUpdate                                 | When there are multiple PUT, each of which update data for the same station ID, the latest data, determined by lamport clock timestamp is registed on the database                   |
|                                         | testCorrectBackUpCreated                                                  | When server creates a backup, another instance can start using the backup to restore the server's state                                                                              |
| ClientTimeOutTest                       | testGETClientRetryAfterTimeOut                                            | When the client does not receive a response from the server after 5 seconds, it retries the process 5 times. If after 5 times nothing happens, an exception is thrown                |
|                                         | testContentServerRetryAfterTimeOut                                        | When the client does not receive a response from the server after 5 seconds, it retries the process 5 times. If after 5 times nothing happens, an exception is thrown                |
| LoadBalancerGETPUTTest                  | testOneGETOnePUTGivesSameResult                                           | When a Load Balancer is used in place of an AggServer, the behaviour is similar to as if an AggServer is used to serve clients                                                       |
|                                         | testInterLeaveGETPUT                                                      | When a Load Balancer is used in place of an AggServer, the behaviour is similar to as if an AggServer is used to serve clients                                                       |
|                                         | testArchiveClearedAfterTimeOut                                            | When a Load Balancer is used in place of an AggServer, the behaviour is similar to as if an AggServer is used to serve clients                                                       |
|                                         | testStaleArchiveRemoved                                                   | When a Load Balancer is used in place of an AggServer, the behaviour is similar to as if an AggServer is used to serve clients                                                       |
|                                         | testBackUpGenerated                                                       | When a Load Balancer is used in place of an AggServer, the behaviour is similar to as if an AggServer is used to serve clients                                                       |
| LoadBalancerFailOverTest                | testWhenBuiltInServerDiesIsAliveIsFalse                                   | When the leader is dead, the heart beat test returns False                                                                                                                           |
|                                         | testAutoFailOverAfterHealthCheck                                          | When the leader is dead, the load balancer automatically recreates another leader                                                                                                    |
| LoadBalancerUsePreSetFailOverServerTest | testBuiltInServerIsNot4568                                                | When another server is set to be the leader, the ID of the leader is the set server                                                                                                  |
|                                         | testLeaderIs4568                                                          | When another server is set to be the leader, the ID of the leader is the set server                                                                                                  |
|                                         | testLeaderIsActive                                                        | When another server is set to be the leader, the ID of the leader is the set server                                                                                                  |
|                                         | testLeaderIsSwappedTo4569WhenDead                                         | When the leader is dead, a new leader is elected                                                                                                                                     |

## UAT Tests

Additional tests were done using the makefile. The results are reported below:

| Test Scenarion                                        | Replication Step                                                                                                                                                                          | Expected Result                                                   | Status |
|-------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|--------|
| Client Server Commmunicate                            | On one terminal type: `make clean`  Then `make load_balancer`  On another terminal type: `make content_server`  Then type:  `make get_client CONTENT_ID=5000`                             | The client result will see json  response  with station_id=5000   | Pass   |
| Client Server Communicate  Browser                    | On one terminal type:  `make load_balancer`  On another terminal type: `make content_server`  Open a browser and type: `127.0.0.1:4567/5000`                                              | Browser shows json data with station_id=5000                      | Pass   |
| Request Non-Exist station id                          | On one terminal type:  `make load_balancer`  On another terminal type: `make content_server`  Open a browser and type: `127.0.0.1:4567/5025`                                              | 404 Response                                                      | Pass   |
| Add a different file and  request a file in database  | On one terminal type:  `make load_balancer`  On another terminal type: `make content_server CONTENT_FILE=Glenelg_2023-07-15_16-30-00.txt`  Open a browser and type: `127.0.0.1:4567/5045` | The client result will see json  response  with station_id=5045   | Pass   |
| Backup is created                                     | On one terminal type:  `make load_balancer`  On another terminal type: `make upload_all`  Then wait 15 minutes                                                                            | There will be backup data generated  at src/resources/FileSystem/ | Pass   |