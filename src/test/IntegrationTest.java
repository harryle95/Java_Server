import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;
import utility.json.Parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

public abstract class IntegrationTest {
    private final int MAX_RETRY = 5;
    AggregationServer server;
    private int retries = 0;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        try {
            server = new AggregationServer("4567".split(" "));
            retries = 0;
            Runnable task = new StartServer(server);
            new Thread(task).start();
        } catch (IOException e) {
            retries += 1;
            if (retries < MAX_RETRY) {
                Thread.sleep(500);
                setUp();
            } else {
                throw new RuntimeException(e);
            }
        }
    }


    @AfterEach
    void shutDown() {
        try {
            server.close();
        } catch (IOException e) {
//            System.out.println("Already Closed");
        }
    }


    static class StartServer implements Runnable {
        AggregationServer server;

        public StartServer(AggregationServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                server.start();
            } catch (IOException e) {
//                System.out.println("Server is already closed");
            }
        }
    }
}

class OneGetOneContentTest extends IntegrationTest {

    @Test
    void testClientRequestingNotFoundID() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567 A0".split(" "));
        client.run();
        assertEquals("""
                GET /A0 HTTP/1.1\r
                Host: 127.0.0.1:4567\r
                Accept: application/json\r
                Lamport-Clock: 1\r
                \r
                """, client.sentMessages.get(0));
        assertEquals("""
                HTTP/1.1 404 Not Found\r
                Content-Type: application/json\r
                Lamport-Clock: 3\r
                \r
                """, client.receivedMessages.get(0));
    }

    @Test
    void testClientRequestingBlank() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567".split(" "));
        client.run();
        assertEquals("""
                GET / HTTP/1.1\r
                Host: 127.0.0.1:4567\r
                Accept: application/json\r
                Lamport-Clock: 1\r
                \r
                """, client.sentMessages.get(0));
        assertEquals("""
                HTTP/1.1 204 No Content\r
                Content-Type: application/json\r
                Lamport-Clock: 3\r
                \r
                """, client.receivedMessages.get(0));
    }

    @Test
    void testClientRequestingFoundID() throws IOException {
        ContentServer.main("127.0.0.1:4567 src/test/utility/json/resources/twoID.txt".split(" "));
        GETClient client = GETClient.from_args("127.0.0.1:4567 A0".split(" "));
        client.run();
        assertEquals("""
                GET /A0 HTTP/1.1\r
                Host: 127.0.0.1:4567\r
                Accept: application/json\r
                Lamport-Clock: 1\r
                \r
                """, client.sentMessages.get(0));
        assertEquals("""
                HTTP/1.1 200 OK\r
                Content-Type: application/json\r
                Content-Length: 62\r
                Lamport-Clock: 9\r
                \r
                {
                "id": "A0",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f"
                }""", client.receivedMessages.get(0));
    }

    @Test
    void testServerShutDownClientThrowingException() {
        assertThrows(IOException.class, () -> {
            server.close();
            GETClient.main("127.0.0.1:4567 A0".split(" "));
        });
    }

    @Test
    void testClientErrorNotShuttingDownServer() {
        assertThrows(RuntimeException.class, () -> GETClient.main(("127.0.0.1:4567 A0" +
                " " +
                "A1").split(" ")));
        assertTrue(server.isUp);
    }

    @Test
    void testClientPrematureClosingDoesNotShutDownServer() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567 A0".split(" "));
        client.close();
        assertTrue(server.isUp);
    }

    @Test
    void testClientCloseDoesCloseSocket() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567 A0".split(" "));
        client.close();
        assertFalse(client.isUp);
    }

    @Test
    void testServerCloseDoesCloseSocket() throws IOException {
        server.close();
        assertFalse(server.isUp);
    }

    @Test
    void testServerHandleMultipleGETClients() throws IOException {
        GETClient.main("127.0.0.1:4567 A0".split(" "));
        GETClient.main("127.0.0.1:4567 A1".split(" "));
        assertTrue(server.isUp);
    }

    @Test
    void testContentServerPUTRequest() throws IOException {
        ContentServer contentServer = ContentServer.from_args(("127.0.0.1:4567 " +
                "src/test/utility/json/resources/twoID.txt").split(" "));
        contentServer.run();
        assertEquals("""
                GET / HTTP/1.1\r
                Host: 127.0.0.1:4567\r
                Accept: application/json\r
                Lamport-Clock: 1\r
                \r
                """, contentServer.sentMessages.get(0));
        assertEquals("""
                HTTP/1.1 204 No Content\r
                Content-Type: application/json\r
                Lamport-Clock: 3\r
                \r
                """, contentServer.receivedMessages.get(0));
        assertEquals("""
                PUT /src/test/utility/json/resources/twoID.txt HTTP/1.1\r
                Host: 127.0.0.1:4567\r
                Accept: application/json\r
                Content-Type: application/json\r
                Content-Length: 122\r
                Lamport-Clock: 5\r
                \r
                {
                "id": "A0",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f",
                "id": "A1",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f"
                }""", contentServer.sentMessages.get(1));
        assertEquals("""
                HTTP/1.1 201 Created\r
                Content-Type: application/json\r
                Content-Length: 122\r
                Lamport-Clock: 7\r
                \r
                {
                "id": "A0",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f",
                "id": "A1",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f"
                }""", contentServer.receivedMessages.get(1));
    }

    @Test
    void testContentServerMultiplePUTRequest() throws IOException {
        ContentServer.main("127.0.0.1:4567 src/test/utility/json/resources/twoID.txt".split(" "));
        ContentServer contentServer = ContentServer.from_args(("127.0.0.1:4567 " +
                "src/test/utility/json/resources/twoID.txt").split(" "));
        contentServer.run();
        assertEquals("""
                GET / HTTP/1.1\r
                Host: 127.0.0.1:4567\r
                Accept: application/json\r
                Lamport-Clock: 1\r
                \r
                """, contentServer.sentMessages.get(0));
        assertEquals("""
                HTTP/1.1 204 No Content\r
                Content-Type: application/json\r
                Lamport-Clock: 9\r
                \r
                """, contentServer.receivedMessages.get(0));
        assertEquals("""
                PUT /src/test/utility/json/resources/twoID.txt HTTP/1.1\r
                Host: 127.0.0.1:4567\r
                Accept: application/json\r
                Content-Type: application/json\r
                Content-Length: 122\r
                Lamport-Clock: 11\r
                \r
                {
                "id": "A0",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f",
                "id": "A1",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f"
                }""", contentServer.sentMessages.get(1));
        assertEquals("""
                HTTP/1.1 200 OK\r
                Content-Type: application/json\r
                Content-Length: 122\r
                Lamport-Clock: 13\r
                \r
                {
                "id": "A0",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f",
                "id": "A1",
                "lat": 10,
                "lon": 20.2,
                "wind_spd_kt": "0x00f"
                }""", contentServer.receivedMessages.get(1));
    }

    @Test
    void testContentServerPUTRequestBeingIdempotent() throws IOException {
        // Run once check results identical
        ContentServer.main(("127.0.0.1:4567 src/resources/SingleEntry/Adelaide_2023" +
                "-07" +
                "-15_16-00-00.txt").split(" "));
        GETClient firstClient = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        firstClient.run();

        HTTPResponse firstResponse =
                HTTPResponse.fromMessage(firstClient.receivedMessages.get(0));
        // Run again, check results are the same
        ContentServer.main(("127.0.0.1:4567 src/resources/SingleEntry/Adelaide_2023" +
                "-07" +
                "-15_16-00-00.txt").split(" "));
        GETClient secondClient = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        secondClient.run();
        HTTPResponse secondResponse =
                HTTPResponse.fromMessage(secondClient.receivedMessages.get(0));
        assertEquals(firstResponse.body, secondResponse.body);
    }

    @Test
    void testClientSendingPOSTRequestReceiveBadRequest() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567".split(" "));
        HTTPRequest request = new HTTPRequest("1.1").setMethod("POST").setURI("/Adelaide");
        client.send(request);
        HTTPResponse response = HTTPResponse.fromMessage(client.receive());
        assertEquals("400", response.statusCode);
    }

}

class MultipleSerialPUTTest extends IntegrationTest {
    Map<String, String> fixtureMap;

    List<String> fileNames;

    @BeforeEach
    void createFixture() throws IOException {
        fixtureMap = new HashMap<>();
        fileNames = new ArrayList<>();
        Parser parser = new Parser();
        String prefixPath = "src/resources/SingleEntry/";
        fileNames.add(prefixPath + "Adelaide_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Adelaide_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Glenelg_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Glenelg_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "HenleyBeach_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "HenleyBeach_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Kilkenny_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Kilkenny_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Melbourne_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Melbourne_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "NorthAdelaide_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "NorthAdelaide_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Parkville_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Parkville_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Pennington_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Pennington_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Seaton_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Seaton_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Semaphore_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Semaphore_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "StClair_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "StClair_2023-07-15_16-30-00.txt");

        for (String path : fileNames) {
            parser.parseFile(Path.of(path));
            fixtureMap.put(path, parser.toString());
        }
    }

    @Test
    void testSinglePUTRequestsUpdateDatabase() throws IOException {
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        assertEquals("{\n" + server.getDatabase().get("5000") + "\n}",
                fixtureMap.get(fileNames.get(0)));
    }

    @Test
    void testMultiplePUTRequestsUpdateDatabase() throws IOException {
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        assertEquals("{\n" + server.getDatabase().get("5000") + "\n}",
                fixtureMap.get(fileNames.get(0)));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(1)).split(" "));
        assertEquals("{\n" + server.getDatabase().get("5000") + "\n}",
                fixtureMap.get(fileNames.get(1)));
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
    void testIndependentPUTDoNotInterfereOneAnother() throws IOException {
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(2)).split(" "));
        assertEquals("{\n" + server.getDatabase().get("5000") + "\n}",
                fixtureMap.get(fileNames.get(0)));
        assertEquals("{\n" + server.getDatabase().get("5045") + "\n}",
                fixtureMap.get(fileNames.get(2)));
    }

    @Test
    void testOldestEventsEjectedFromArchive() throws IOException {
        for (String file : fileNames) {
            ContentServer.main(("127.0.0.1:4567 " + file).split(" "));
        }
        assertFalse(server.getArchive().get("/127.0.0.1").containsKey(fileNames.get(0)));
        assertFalse(server.getArchive().get("/127.0.0.1").containsKey(fileNames.get(1)));
        assertTrue(server.getArchive().get("/127.0.0.1").containsKey(fileNames.get(20)));
    }

    @Test
    void testFilesRemovedAfterConnectionClosed() throws IOException, InterruptedException {
        server.setWAIT_TIME(1);
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        assertTrue(server.getArchive().get("/127.0.0.1").containsKey(fileNames.get(0)));
        Thread.sleep(300);
        assertFalse(server.getArchive().get("/127.0.0.1").containsKey(fileNames.get(0)));
    }

    @Test
    void testWhenUpdateQueueContainsTheSameEntryArchiveDoNotRemove() throws IOException {
        server.setFRESH_PERIOD_COUNT(1);
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        assertTrue(server.getArchive().get("/127.0.0.1").containsKey(fileNames.get(0)));
    }

    @Test
    void testWhenUpdateQueueContainsUpdatedVersionOfTheSameEntryArchiveDoNotRemove() throws IOException {
        server.setFRESH_PERIOD_COUNT(3);
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(2)).split(" "));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(1)).split(" "));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        assertTrue(server.getArchive().get("/127.0.0.1").containsKey(fileNames.get(0)));
    }

    @Test
    void testAllFilesRemovedSteadyState() throws InterruptedException {
        server.setWAIT_TIME(1);
        Runnable task1 = () -> {
            try {
                ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable task2 = () -> {
            try {
                ContentServer.main(("127.0.0.1:4567 " + fileNames.get(1)).split(" "));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable task3 = () -> {
            try {
                ContentServer.main(("127.0.0.1:4567 " + fileNames.get(3)).split(" "));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable task4 = () -> {
            try {
                ContentServer.main(("127.0.0.1:4567 " + fileNames.get(4)).split(" "));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        new Thread(task1).start();
        new Thread(task2).start();
        new Thread(task3).start();
        new Thread(task4).start();

        Thread.sleep(500);
        assertTrue(server.getArchive().get("/127.0.0.1").isEmpty());
    }

    @Test
    void testPUTRequestAreSerialisedCorrectlyFourTasks() throws IOException {
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(0)).split(" "));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(1)).split(" "));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(2)).split(" "));
        ContentServer.main(("127.0.0.1:4567 " + fileNames.get(3)).split(" "));

        GETClient firstClient = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        firstClient.run();
        assertEquals(HTTPResponse.fromMessage(firstClient.receivedMessages.get(0)).body, fixtureMap.get(fileNames.get(1)));

        GETClient secondClient = GETClient.from_args("127.0.0.1:4567 5045".split(" "));
        secondClient.run();
        assertEquals(HTTPResponse.fromMessage(secondClient.receivedMessages.get(0)).body, fixtureMap.get(fileNames.get(3)));


    }

}

class MultiplePUTWithCompositeDataTest extends IntegrationTest{
    Map<String, String> fixtureMap;

    List<String> fileNames;
    List<String> fileNamesComposite;
    @BeforeEach
    void createFixture() throws IOException {
        fixtureMap = new HashMap<>();
        fileNames = new ArrayList<>();
        fileNamesComposite = new ArrayList<>();

        Parser parser = new Parser();
        fileNames.add("src/resources/SingleEntry/Adelaide_2023-07-15_16-30-00.txt");
        fileNames.add("src/resources/SingleEntry/Glenelg_2023-07-15_16-30-00.txt");
        fileNames.add("src/resources/SingleEntry/HenleyBeach_2023-07-15_16-30-00.txt");
        fileNames.add("src/resources/SingleEntry/Glenelg_2023-07-15_16-00-00.txt");

        parser.parseFile(Path.of(fileNames.get(0)));
        fixtureMap.put("5000", parser.toString());

        parser.parseFile(Path.of(fileNames.get(1)));
        fixtureMap.put("5045_New", parser.toString());

        parser.parseFile(Path.of(fileNames.get(2)));
        fixtureMap.put("5022", parser.toString());

        parser.parseFile(Path.of(fileNames.get(3)));
        fixtureMap.put("5045_Old", parser.toString());

        fileNamesComposite.add("src/resources/Composite/Adelaide_2023-07-15_16-30-00.txt");
        fileNamesComposite.add("src/resources/Composite/Glenelg_2023-07-15_16-30-00.txt");
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

}