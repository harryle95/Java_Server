import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {
    AggregationServer server;
    private int retries = 0;

    private final int MAXRETRIES = 5;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        try {
            server = new AggregationServer("4567".split(" "));
            retries = 0;
            Runnable task = new StartServer(server);
            new Thread(task).start();
        } catch (IOException e) {
            retries += 1;
            if (retries < MAXRETRIES) {
                Thread.sleep(500);
                setUp();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

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
    void testServerShutDownClientThrowingException(){
        assertThrows(IOException.class, ()->{
            server.close();
            GETClient.main("127.0.0.1:4567 A0".split(" "));
        });
    }

    @Test
    void testClientErrorNotShuttingDownServer(){
        assertThrows(RuntimeException.class, ()-> GETClient.main("127.0.0.1:4567 A0 A1".split(" ")));
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


    @AfterEach
    void shutDown() {
        try {
            server.close();
        } catch (IOException e) {
            System.out.println("Already Closed");
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
                System.out.println("Server is already closed");
            }
        }
    }
}