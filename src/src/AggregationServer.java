import utility.LamportClock;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class AggregationServer {
    private final Map<String, String> database;
    private final Map<String, Map<String, Map<String, String>>> archive;


    private final ExecutorService connectionPool; // Threadpool to accept incoming requests

    private final ThreadPoolExecutor requestHandlerPool;

    private final ScheduledExecutorService backupPool;

    private final int POOLSIZE = 10;


    private ServerSocket serverSocket;
    private final LamportClock clock;


    public AggregationServer(String[] argv) throws IOException {
        int port = getPort(argv);
        database = new HashMap<>();
        archive = new HashMap<>();
        connectionPool = Executors.newCachedThreadPool();
        backupPool = Executors.newScheduledThreadPool(POOLSIZE);


        clock = new LamportClock();
        run(port);
    }

    public void run(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        System.out.println("Server listening at port: " + port);
    }

    public void start() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();

            // Connection Pool listen for incoming requests
            connectionPool.execute(new ConnectionHandler(clientSocket, clock));
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

    public static void main(String[] args) throws IOException {
        AggregationServer server = new AggregationServer(args);
        server.start();
        server.close();
    }

    public void close() throws IOException {
        serverSocket.close();
    }
}


