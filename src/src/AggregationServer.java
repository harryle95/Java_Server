import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class AggregationServer {
    private ServerSocket serverSocket;

    public void run(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        System.out.println("Server listening at port: " + port);
    }

    public void start() {
        while (true) {
            try (
                    Socket clientSocket = serverSocket.accept();
            ) {
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int getPort(String[] args) {
        int port;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 0) {
            port = 4567;
        } else {
            throw new RuntimeException("Usage: AggregationServer [port].");
        }
        return port;
    }

    public static void main(String[] args) {
        int port = getPort(args);
        try {
            AggregationServer server = new AggregationServer();
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
            Thread currentThread = Thread.currentThread();
            String name = currentThread.getName();
            long id = currentThread.getId();
            String threadInfo = name + "." + String.valueOf(id);
            System.out.println("Spawning a new socket: " + clientSocket.getLocalPort());
            System.out.println("Accepting Connection from: " + clientSocket.getRemoteSocketAddress());
            System.out.println("ThreadID: " + threadInfo);
            String clientInput, serverInput;
            while (true) {
                clientInput = in.readLine();
                System.out.println(clientInput);
                out.println("Server Response");
//                if ((clientInput == null) | (clientInput != null && clientInput.equals("exit"))) {
//                    break;
//                }
//                if (!clientInput.isEmpty()) {
//                    System.out.println(threadInfo+ ">>" + clientInput);
//                    out.println(clientInput);
//                }
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