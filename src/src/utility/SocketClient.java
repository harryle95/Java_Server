package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient extends SocketCommunicator {

    public SocketClient() {
        super();
        this.type = "client";
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String hostname;


    private int port;


    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public void connect() {
        try {
            clientSocket = new Socket(hostname, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("My connection info: " + clientSocket.getLocalSocketAddress());
            System.out.println("Connecting to: " + clientSocket.getRemoteSocketAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException("No host at " + hostname + ":" + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}