import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrationTest {
    AggregationServer server;

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
                System.out.println("Server is closed");
            }
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        server = new AggregationServer("4567".split(" "));
        Runnable task = new StartServer(server);
        new Thread(task).start();
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
    void testContentServerPUTRequest() throws IOException{
        ContentServer contentServer = ContentServer.from_args("127.0.0.1:4567 src/test/utility/json/resources/twoID.txt".split(" "));
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


    @AfterEach
    void shutDown() throws IOException {
        server.close();
    }
}