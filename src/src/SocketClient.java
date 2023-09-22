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
        } catch (UnknownHostException e) {
            throw new RuntimeException("No host at " + hostname + ":" + port);
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

    public void sendMessage(String message) {
        out.println(message);
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }
}
