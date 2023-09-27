import utility.LamportClock;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class AggregationServer {
    private final ConcurrentMap<String, String> database;
    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive;


    private final ExecutorService connectionPool; // Threadpool to accept incoming requests

    private final ExecutorService requestHandlerPool;

    private final ScheduledExecutorService backupPool;

    private final int POOLSIZE = 10;


    private ServerSocket serverSocket;
    private final LamportClock clock;


    public AggregationServer(String[] argv) throws IOException {
        int port = getPort(argv);
        database = new ConcurrentHashMap<>();
        archive = new ConcurrentHashMap<>();
        connectionPool = Executors.newCachedThreadPool();
        backupPool = Executors.newScheduledThreadPool(POOLSIZE);
        requestHandlerPool = new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(10, new PriorityRunnableFutureComparator())
        ) {
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
                return new PriorityRunnableFuture<>(newTaskFor, ((RequestHandler) callable).getPriority());
            }
        };

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
            connectionPool.execute(new ConnectionHandler(
                    clientSocket, clock, database, archive, requestHandlerPool));
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


