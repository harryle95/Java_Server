import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class GreetServer {
    private ServerSocket serverSocket;

    public void run(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        System.out.println("Server listening at port: " + port);
    }

    public void start() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket);
            new Thread(handler).start();
        }
    }


    public static void main(String[] args) {
        int port;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 0) {
            port = 4567;
        } else {
            throw new RuntimeException("Usage: AggregationServer [port].");
        }
        try {
            GreetServer server = new GreetServer();
            server.run(port);
            server.start();
        } catch (BindException e) {
            System.out.println("Port already in use: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            System.out.println("Spawning a new socket: " + clientSocket.getLocalPort());
            System.out.println("Accepting Connection from: " + clientSocket.getRemoteSocketAddress());
            String clientInput, serverInput;
            while (true) {
                clientInput = in.readLine();
                if ((clientInput == null) | (clientInput != null && clientInput.equals("exit"))) {
                    break;
                }
                if (!clientInput.isEmpty()) {
                    System.out.println(">>" + clientInput);
                    out.println(clientInput);
                }
            }
        } catch (SocketException e) {
            System.out.println("Connection aborted. Client is disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}