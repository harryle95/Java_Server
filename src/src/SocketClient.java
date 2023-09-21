import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {
    Socket clientSocket;
    PrintWriter out;
    BufferedReader in;
    private String URL;

    public String getServerName() {
        return serverName;
    }

    public int getPort() {
        return port;
    }

    private String serverName;

    private int port;

    public void parse(String URL) {
        String[] split_string = URL.split(":");
        if (split_string.length == 2) {
            this.serverName = split_string[0];
            this.port = Integer.parseInt(split_string[1]);
        } else if (split_string.length != 3) {
            throw new RuntimeException("URL must be server:port or http://server:port or http://server.domain:port");
        } else {
            this.port = Integer.parseInt(split_string[2]);
            String _serverName = split_string[1].split("\\.")[0];
            if (!_serverName.startsWith("//")) {
                throw new RuntimeException("URL must be server:port or http://server:port or http://server.domain:port");
            }
            this.serverName = _serverName.substring(2);
        }
        System.out.println(serverName + ":" + port);
    }

    public SocketClient(String URL) {
        parse(URL);
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
