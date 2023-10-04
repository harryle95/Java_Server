import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utility.ServerSnapshot;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;
import utility.weatherJson.Parser;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest extends BaseTest {
    private final int MAX_RETRY = 5;
    private final int DEFAULT_WAIT_TIME = 100;
    AggregationServer server;
    private int retries = 0;


    @Override
    void setupHook() {
        try {
            server = new AggregationServer(port);
            retries = 0;
        } catch (IOException e) {
            retries += 1;
            if (retries < MAX_RETRY) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    logger.info("Error: for setup hook: " + this.getClass().getName() +
                                ": " + e);
                }
                setupHook();
            }
        } catch (ClassNotFoundException e) {
            logger.info("Error: for setup hook: " + this.getClass().getName() + ": " + e);
        }
        server.setWAIT_TIME(DEFAULT_WAIT_TIME);
        threadPool.submit(new StartServer(server));
    }

    @Override
    void shutdownHook() {
        server.close();
    }

    static class StartServer implements Runnable {
        AggregationServer server;

        public StartServer(AggregationServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            server.start();
        }
    }
}

class HTTPErrorMessageHandlingTest extends IntegrationTest {

    @Test
    void testRawPUTMessageWorks() throws IOException, InterruptedException {
        ContentServer contentServer = ContentServer.from_args(("127.0.0.1:4567 " +
                "src/test/utility" +
                "/weatherJson" +
                "/resources/twoID.txt").split(" "));
        HTTPRequest request = contentServer.formatPUTMessage();
        contentServer.send(request);
        String message = contentServer.receive();
        assertEquals(request.body, HTTPResponse.fromMessage(message).body);
    }

    @Test
    void testGETNotFoundIDGives404NotFound() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567 A0".split(" "));
        client.run();
        assertEquals("GET /A0 HTTP/1.1\r\n" +
                     "Host: 127.0.0.1:4567\r\n" +
                     "Accept: application/json\r\n" +
                     "Lamport-Clock: 1\r\n" +
                     "\r\n", client.sentMessages.get(0));
        HTTPResponse response = HTTPResponse.fromMessage(client.receivedMessages.get(0));
        assertEquals("404", response.statusCode);
    }

    @Test
    void testGETBlankGives204NoContent() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567".split(" "));
        client.run();
        assertEquals("GET / HTTP/1.1\r\n" +
                     "Host: 127.0.0.1:4567\r\n" +
                     "Accept: application/json\r\n" +
                     "Lamport-Clock: 1\r\n" +
                     "\r\n", client.sentMessages.get(0));
        HTTPResponse response = HTTPResponse.fromMessage(client.receivedMessages.get(0));
        assertEquals("204", response.statusCode);
    }

    @Test
    void testGETFoundIDGives200OK() throws IOException {
        ContentServer.main(("127.0.0.1:4567 src/test/utility/weatherJson/resources" +
                            "/twoID.txt").split(" "));
        GETClient client = GETClient.from_args("127.0.0.1:4567 A0".split(" "));
        client.run();
        assertEquals("GET /A0 HTTP/1.1\r\n" +
                     "Host: 127.0.0.1:4567\r\n" +
                     "Accept: application/json\r\n" +
                     "Lamport-Clock: 1\r\n" +
                     "\r\n", client.sentMessages.get(0));
        assertEquals("HTTP/1.1 200 OK\r\n" +
                     "Content-Type: application/json\r\n" +
                     "Content-Length: 62\r\n" +
                     "Lamport-Clock: 9\r\n" +
                     "\r\n" +
                     "{\n" +
                     "\"id\": \"A0\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\"\n" +
                     "}", client.receivedMessages.get(0));
    }

    @Test
    void testWhenServerShutsDownClientThrowsException() {
        assertThrows(IOException.class, () -> {
            server.close();
            GETClient.main("127.0.0.1:4567 A0".split(" "));
        });
    }

    @Test
    void testClientErrorDoesNotShutDownServer() {
        assertThrows(RuntimeException.class, () -> GETClient.main(("127.0.0.1:4567 A0" +
                                                                   " " +
                                                                   "A1").split(" ")));
        assertTrue(server.isUp());
    }

    @Test
    void testClientPrematureCloseDoesNotShutDownServer() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567 A0".split(" "));
        client.send(client.formatGETMessage());
        client.close();
        assertTrue(server.isUp());
    }

    @Test
    void testClientCloseDoesCloseSocket() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567 A0".split(" "));
        client.close();
        assertFalse(client.isUp);
    }

    @Test
    void testServerCloseDoesCloseSocket() {
        server.close();
        assertFalse(server.isUp());
    }

    @Test
    void testServerHandleMultipleGETClientsSuccessfully() throws IOException {
        GETClient.main("127.0.0.1:4567 A0".split(" "));
        GETClient.main("127.0.0.1:4567 A1".split(" "));
        assertTrue(server.isUp());
    }

    @Test
    void testFirstPUTRequestGives201Created() throws IOException {
        ContentServer contentServer = ContentServer.from_args(("127.0.0.1:4567 " +
                                                               "src/test/utility" +
                                                               "/weatherJson" +
                                                               "/resources/twoID.txt").split(" "));
        contentServer.run();
        assertEquals("GET / HTTP/1.1\r\n" +
                     "Host: 127.0.0.1:4567\r\n" +
                     "Accept: application/json\r\n" +
                     "Lamport-Clock: 1\r\n" +
                     "\r\n", contentServer.sentMessages.get(0));
        HTTPResponse response = HTTPResponse.fromMessage(contentServer.receivedMessages.get(0));
        assertEquals("204", response.statusCode);
        assertEquals("PUT /src/test/utility/weatherJson/resources/twoID.txt HTTP/1" +
                     ".1\r\n" +
                     "Host: 127.0.0.1:4567\r\n" +
                     "Accept: application/json\r\n" +
                     "Content-Type: application/json\r\n" +
                     "Content-Length: 122\r\n" +
                     "Lamport-Clock: 5\r\n" +
                     "\r\n" +
                     "{\n" +
                     "\"id\": \"A0\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\",\n" +
                     "\"id\": \"A1\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\"\n" +
                     "}", contentServer.sentMessages.get(1));
        assertEquals("HTTP/1.1 201 Created\r\n" +
                     "Content-Type: application/json\r\n" +
                     "Content-Length: 122\r\n" +
                     "Lamport-Clock: 7\r\n" +
                     "\r\n" +
                     "{\n" +
                     "\"id\": \"A0\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\",\n" +
                     "\"id\": \"A1\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\"\n" +
                     "}", contentServer.receivedMessages.get(1));
    }

    @Test
    void testSubsequentPUTRequestGives200OK() throws IOException {
        ContentServer.main(("127.0.0.1:4567 src/test/utility/weatherJson/resources" +
                            "/twoID.txt").split(" "));
        ContentServer contentServer = ContentServer.from_args(("127.0.0.1:4567 " +
                                                               "src/test/utility" +
                                                               "/weatherJson" +
                                                               "/resources/twoID.txt").split(" "));
        contentServer.run();
        assertEquals("GET / HTTP/1.1\r\n" +
                     "Host: 127.0.0.1:4567\r\n" +
                     "Accept: application/json\r\n" +
                     "Lamport-Clock: 1\r\n" +
                     "\r\n", contentServer.sentMessages.get(0));
        HTTPResponse response = HTTPResponse.fromMessage(contentServer.receivedMessages.get(0));
        assertEquals("204", response.statusCode );
        assertEquals("PUT /src/test/utility/weatherJson/resources/twoID.txt HTTP/1" +
                     ".1\r\n" +
                     "Host: 127.0.0.1:4567\r\n" +
                     "Accept: application/json\r\n" +
                     "Content-Type: application/json\r\n" +
                     "Content-Length: 122\r\n" +
                     "Lamport-Clock: 11\r\n" +
                     "\r\n" +
                     "{\n" +
                     "\"id\": \"A0\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\",\n" +
                     "\"id\": \"A1\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\"\n" +
                     "}", contentServer.sentMessages.get(1));
        assertEquals("HTTP/1.1 200 OK\r\n" +
                     "Content-Type: application/json\r\n" +
                     "Content-Length: 122\r\n" +
                     "Lamport-Clock: 13\r\n" +
                     "\r\n" +
                     "{\n" +
                     "\"id\": \"A0\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\",\n" +
                     "\"id\": \"A1\",\n" +
                     "\"lat\": 10,\n" +
                     "\"lon\": 20.2,\n" +
                     "\"wind_spd_kt\": \"0x00f\"\n" +
                     "}", contentServer.receivedMessages.get(1));
    }

    @Test
    void testPUTRequestBeingIdempotent() throws IOException {
        // Run once check results identical
        ContentServer.main(("127.0.0.1:4567 src/resources/WeatherData/SingleEntry" +
                            "/Adelaide_2023" +
                            "-07" +
                            "-15_16-00-00.txt").split(" "));
        GETClient firstClient = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        firstClient.run();

        HTTPResponse firstResponse =
                HTTPResponse.fromMessage(firstClient.receivedMessages.get(0));
        // Run again, check results are the same
        ContentServer.main(("127.0.0.1:4567 src/resources/WeatherData/SingleEntry" +
                            "/Adelaide_2023" +
                            "-07" +
                            "-15_16-00-00.txt").split(" "));
        GETClient secondClient = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        secondClient.run();
        HTTPResponse secondResponse =
                HTTPResponse.fromMessage(secondClient.receivedMessages.get(0));
        assertEquals(firstResponse.body, secondResponse.body);
    }

    @Test
    void testPOSTRequestReceives400BadRequest() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567".split(" "));
        HTTPRequest request = new HTTPRequest("1.1").setMethod("POST").setURI(
                "/Adelaide");
        client.send(request);
        HTTPResponse response = HTTPResponse.fromMessage(client.receive());
        assertEquals("400", response.statusCode);
        client.close();
    }

}

class MultipleSerialPUTTest extends IntegrationTest {

    String getDBContent(int id) {
        return "{\n" + server.getDatabase().get(stationID.get(id)) + "\n}";
    }

    ConcurrentMap<String, ConcurrentMap<String, String>> getArchiveContent() {
        return server.getArchive().get("/" + hostname);
    }

    @Test
    void testPUTRequestsUpdateDatabase() throws Exception {
        for (int i = 0; i < fileNames.size(); i++) {
            putRequest(i).call();
            assertEquals(getDBContent(i),
                    fixtureMap.get(fileNames.get(i)));
        }

    }


    @Test
    void testInterleavedGETPUTRequests() throws IOException {
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        GETClient firstClient = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        firstClient.run();
        assertEquals(HTTPResponse.fromMessage(firstClient.receivedMessages.get(0)).body, fixtureMap.get(fileNames.get(0)));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(1)).split(" "));
        GETClient secondClient = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        secondClient.run();
        assertEquals(HTTPResponse.fromMessage(secondClient.receivedMessages.get(0)).body, fixtureMap.get(fileNames.get(1)));
    }

    @Test
    void testIndependentPUTDoNotInterfereOneAnotherSerial() throws Exception {
        for (int i = 0; i < fileNames.size(); i += 2) {
            putRequest(i).call();
        }

        for (int i = 0; i < fileNames.size(); i += 2) {
            assertEquals(getDBContent(i), fixtureMap.get(fileNames.get(i)));
        }
    }

    @Test
    void testIndependentPUTDoNotInterfereOneAnotherConcurrent() throws Exception {
        List<Callable<Object>> taskList = new ArrayList<>();
        for (int i = 0; i < fileNames.size(); i += 2) {
            taskList.add(putRequest(i));
        }
        threadPool.invokeAll(taskList);

        for (int i = 0; i < fileNames.size(); i += 2) {
            assertEquals(getDBContent(i), fixtureMap.get(fileNames.get(i)));
        }
    }

    @Test
    void testOldestEventsEjectedFromArchive() throws Exception {
        for (int i = 0; i < fileNames.size(); i++) {
            putRequest(i).call();
        }
        assertFalse(getArchiveContent().containsKey(fileNames.get(0)));
        assertFalse(getArchiveContent().containsKey(fileNames.get(1)));
        assertTrue(getArchiveContent().containsKey(fileNames.get(20)));
    }

    @Test
    void testFilesRemovedAfterConnectionClosed() throws Exception {
        server.setWAIT_TIME(1);
        putRequest(0).call();
        assertTrue(getArchiveContent().containsKey(fileNames.get(0)));
        Thread.sleep(300);
        assertFalse(getArchiveContent().containsKey(fileNames.get(0)));
    }

    @Test
    void testWhenUpdateQueueContainsTheSameEntryArchiveDoNotRemove() throws Exception {
        server.setFRESH_PERIOD_COUNT(1);
        putRequest(0).call();
        putRequest(0).call();
        assertTrue(getArchiveContent().containsKey(fileNames.get(0)));
    }

    @Test
    void testWhenUpdateQueueContainsUpdatedVersionOfTheSameEntryArchiveDoNotRemove() throws Exception {
        server.setFRESH_PERIOD_COUNT(3);
        putRequest(0).call();
        putRequest(1).call();
        putRequest(2).call();
        putRequest(0).call();
        assertTrue(server.getArchive().get("/127.0.0.1").containsKey(fileNames.get(0)));
    }


    @Test
    void testAllFilesRemovedSteadStateAllFiles() throws InterruptedException {
        server.setWAIT_TIME(1);
        List<Callable<Object>> taskList = new ArrayList<>();
        for (int i = 0; i < fileNames.size(); i += 2) {
            taskList.add(putRequest(i));
        }
        threadPool.invokeAll(taskList);
        Thread.sleep(1000);
        assertTrue(server.getArchive().get("/127.0.0.1").isEmpty());
    }

    @Test
    void testPUTRequestAreSerialisedCorrectlyFourTasks() throws Exception {
        putRequest(0).call();
        putRequest(1).call();
        putRequest(2).call();
        putRequest(3).call();

        HTTPResponse response = getResponse(stationID.get(0));
        assertEquals(response.body, fixtureMap.get(fileNames.get(1)));

        response = getResponse(stationID.get(3));
        assertEquals(response.body, fixtureMap.get(fileNames.get(3)));
    }

}

class MultiplePUTWithCompositeDataTest extends IntegrationTest {
    Map<String, String> fixtureMap;

    List<String> fileNames;
    List<String> fileNamesComposite;

    @BeforeEach
    void overwriteFixture() throws IOException {
        fixtureMap = new HashMap<>();
        fileNames = new ArrayList<>();
        fileNamesComposite = new ArrayList<>();

        Parser parser = new Parser();
        fileNames.add("src/resources/WeatherData/SingleEntry/Adelaide_2023-07-15_16" +
                      "-30-00.txt");
        fileNames.add("src/resources/WeatherData/SingleEntry/Glenelg_2023-07-15_16-30" +
                      "-00.txt");
        fileNames.add("src/resources/WeatherData/SingleEntry/HenleyBeach_2023-07" +
                      "-15_16-30-00.txt");
        fileNames.add("src/resources/WeatherData/SingleEntry/Glenelg_2023-07-15_16-00" +
                      "-00.txt");

        parser.parseFile(Paths.get(fileNames.get(0)));
        fixtureMap.put("5000", parser.toString());

        parser.parseFile(Paths.get(fileNames.get(1)));
        fixtureMap.put("5045_New", parser.toString());

        parser.parseFile(Paths.get(fileNames.get(2)));
        fixtureMap.put("5022", parser.toString());

        parser.parseFile(Paths.get(fileNames.get(3)));
        fixtureMap.put("5045_Old", parser.toString());

        fileNamesComposite.add("src/resources/WeatherData/Composite/Adelaide_2023-07" +
                               "-15_16-30-00" +
                               ".txt");
        fileNamesComposite.add("src/resources/WeatherData/Composite/Glenelg_2023-07" +
                               "-15_16-30-00" +
                               ".txt");
    }

    @Test
    void testCompositePUTUseLatestTimer() throws IOException {
        ContentServer.main(("127.0.0.1:4567 " + fileNamesComposite.get(0)).split(" "));
        GETClient firstClient = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        firstClient.run();
        assertEquals(HTTPResponse.fromMessage(firstClient.receivedMessages.get(0)).body, fixtureMap.get("5000"));
        ContentServer.main(("127.0.0.1:4567 " + fileNamesComposite.get(1)).split(" "));
        GETClient secondClient = GETClient.from_args("127.0.0.1:4567 5022".split(" "));
        secondClient.run();
        assertEquals(HTTPResponse.fromMessage(secondClient.receivedMessages.get(0)).body, fixtureMap.get("5022"));
    }

    @Test
    void testConsecutivePUTOnSameIDUseLatestUpdate() throws IOException {
        ContentServer.main(("127.0.0.1:4567 " + fileNamesComposite.get(0)).split(" "));
        GETClient firstClient = GETClient.from_args("127.0.0.1:4567 5045".split(" "));
        firstClient.run();
        assertEquals(HTTPResponse.fromMessage(firstClient.receivedMessages.get(0)).body, fixtureMap.get("5045_Old"));
        ContentServer.main(("127.0.0.1:4567 " + fileNamesComposite.get(1)).split(" "));
        GETClient secondClient = GETClient.from_args("127.0.0.1:4567 5045".split(" "));
        secondClient.run();
        assertEquals(HTTPResponse.fromMessage(secondClient.receivedMessages.get(0)).body, fixtureMap.get("5045_New"));
    }

    @Test
    void testCorrectBackUpCreated() throws Exception {
        server.setWAIT_TIME(3000);
        putRequest(0).call();
        server.getServerSnapshot().createSnapShot();
        ServerSnapshot newSnapShot = new ServerSnapshot(databaseBackUp, archiveBackUp);
        assertEquals(server.getDatabase(), newSnapShot.getDatabase());
        assertEquals(server.getArchive(), newSnapShot.getArchive());

        // Restart the server
        shutdown();
        setupNotDelete();
        // Test
        assertEquals(server.getDatabase(), newSnapShot.getDatabase());
        assertEquals(server.getArchive(), newSnapShot.getArchive());
        // Delete Files
        deleteFiles();
    }
}
