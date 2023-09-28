import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utility.MessageExchanger;
import utility.http.HTTPRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public abstract class GETClientTest {
    @Mock
    Socket clientSocket;

    @Mock
    PrintWriter out;

    @Mock
    BufferedReader in;

    String host;

    int port;

    String stationID;

    GETClient client;

    @BeforeEach
    void setUp() throws IOException {
        clientSocket = mock(Socket.class);
        out = mock(PrintWriter.class);
        in = mock(BufferedReader.class);
    }

}

@ExtendWith(MockitoExtension.class)
class GETClientNonExistIDTest extends GETClientTest {
    String response;
    String encodedResponse;

    String request;
    String encodedRequest;


    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        host = "localhost";
        port = 4567;
        stationID = "nonExistID";
        client = new GETClient(clientSocket, out, in, host, port, stationID);

        request = """
                GET /nonExistID HTTP/1.1\r
                Host: localhost:4567\r
                Accept: application/json\r
                \r
                """;
        encodedRequest = MessageExchanger.encode(request);

        response = """
                HTTP/1.1 404 Not Found\r
                Content-Type: application/json\r
                Lamport-Clock: 3\r
                \r
                                
                """;
        encodedResponse = MessageExchanger.encode(response);
    }


    @Test
    void testGetStationID(){
        assertEquals("nonExistID", client.getStationID());
    }


    @Test
    void testFormatMessage() {
        assertEquals(request, client.formatMessage().toString());
    }

    @Test
    void testSend() {
        HTTPRequest request = client.formatMessage();
        client.send(request);
        verify(out, times(1)).println(anyString());
    }

    @Test
    void testReceive() throws IOException {
        client.receive();
        verify(in, times(1)).readLine();
    }

    @Test
    void testRun() throws IOException {
        when(in.readLine()).thenReturn(encodedResponse);
        client.run();
        verify(in, times(1)).readLine();
        verify(out, times(1)).println(anyString());
        verify(clientSocket, times(1)).close();
    }


}

@ExtendWith(MockitoExtension.class)
class GETClientWithoutIDTest extends GETClientTest {

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        host = "localhost";
        port = 4567;
        stationID = null;
        client = new GETClient(clientSocket, out, in, host, port, stationID);
    }

    @Test
    void testGetStationID(){
        assertNull(client.getStationID());
    }

    @Test
    void testFormatMessage() {
        assertEquals("""
                GET / HTTP/1.1\r
                Host: localhost:4567\r
                Accept: application/json\r
                \r
                """, client.formatMessage().toString());
    }
}

