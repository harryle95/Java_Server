import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SimulateServerErrorTest {
    ServerSocket serverSocket;
    Socket receivingSocket;
    BufferedReader in;
    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(4567);
        new Thread(() -> {
            while (true) {
                try {
                    receivingSocket = serverSocket.accept();
                    in = new BufferedReader(new InputStreamReader(receivingSocket.getInputStream()));
                    while (in.readLine() != null) {
                    }
                } catch (IOException e) {
//                    System.out.println("Connection is Closed");
                }
            }
        }).start();
    }
    @AfterEach
    void shutDown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
//            System.out.println(e.getMessage());
        }
    }

}

class GETClientTimeOutTest extends SimulateServerErrorTest {

    @Test
    void testGETClientRetryAfterTimeOut() throws IOException {
        GETClient client = GETClient.from_args("127.0.0.1:4567 5000".split(" "));
        client.setMAX_RETRY(3);
        client.setSO_TIMEOUT(100);
        assertThrows(RuntimeException.class, client::run);
        assertEquals(3, client.sentMessages.size());
    }

    @Test
    void testContentServerRetryAfterTimeOut() throws IOException {
        ContentServer contentServer = ContentServer.from_args("127.0.0.1:4567 src/resources/SingleEntry/Adelaide_2023-07-15_16-30-00.txt".split(" "));
        contentServer.setMAX_RETRY(3);
        contentServer.setSO_TIMEOUT(100);
        assertThrows(RuntimeException.class, contentServer::run);
        assertEquals(3, contentServer.sentMessages.size());
    }

}
