package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient extends SocketCommunicator {
    public SocketClient(
            Socket clientSocket,
            PrintWriter out,
            BufferedReader in) throws IOException {
        super(
                clientSocket,
                new LamportClock(),
                out,
                in,
                "client");
    }


    protected String hostname;


    protected int port;


    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

}