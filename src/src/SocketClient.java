import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class SocketClient {
    Socket clientSocket;
    PrintWriter out;
    BufferedReader in;

    private String serverName;

    private int port;

    public SocketClient(String[] argv) {
    }

    public String getServerName() {
        return serverName;
    }

    public int getPort() {
        return port;
    }

    public void connect() {
        try {
            clientSocket = new Socket(serverName, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            throw new RuntimeException("No host at " + serverName + ":" + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Socket already closed");
        }
    }

}
