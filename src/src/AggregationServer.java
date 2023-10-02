import annotations.IgnoreCoverage;
import handlers.ConnectionHandler;
import handlers.PriorityRunnableFuture;
import handlers.PriorityRunnableFutureComparator;
import handlers.RequestHandler;
import utility.FileMetadata;
import utility.LamportClock;
import utility.ServerSnapshot;
import utility.SocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.*;

public class AggregationServer extends SocketServer {
    private final ConcurrentMap<String, String> database;
    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String,
            String>>> archive;
    private final LinkedBlockingQueue<FileMetadata> updateQueue; // Queue referencing
    private final ExecutorService connectionHandlerPool; // Thread pool to accept
    private final ExecutorService requestHandlerPool; // Thread pool to handle request
    // archive data based on order of update
    private final ScheduledExecutorService schedulePool; // Thread pool to execute

    public ServerSnapshot getServerSnapshot() {
        return serverSnapshot;
    }

    private final ServerSnapshot serverSnapshot; // Server snapshot service

    // incoming requests
    private final int POOL_SIZE = 20;
    private final LamportClock clock;
    private int FRESH_PERIOD_COUNT = 20; // how many updates until the current is no longer fresh
    private int WAIT_TIME = 30000; // how long to wait until the cleanup task - 30 seconds
    private final int BACKUP_TIME = 15; // time between auto backup - default 15 minutes

    public AggregationServer(int port) throws IOException, ClassNotFoundException {
        super(port);
        serverSnapshot = new ServerSnapshot();
        database = serverSnapshot.getDatabase();
        archive = serverSnapshot.getArchive();
        connectionHandlerPool = Executors.newCachedThreadPool();
        schedulePool = Executors.newScheduledThreadPool(POOL_SIZE);
        updateQueue = new LinkedBlockingQueue<>();
        requestHandlerPool = new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(10, new PriorityRunnableFutureComparator())
        ) {
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
                return new PriorityRunnableFuture<>(newTaskFor,
                        ((RequestHandler) callable).getPriority());
            }
        };

        clock = new LamportClock();
        run();
    }


    @IgnoreCoverage
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int port = getPort(args);
        AggregationServer server = new AggregationServer(port);
        server.start();
        server.close();
    }

    public void setFRESH_PERIOD_COUNT(int FRESH_PERIOD_COUNT) {
        this.FRESH_PERIOD_COUNT = FRESH_PERIOD_COUNT;
    }

    public void setWAIT_TIME(int WAIT_TIME) {
        this.WAIT_TIME = WAIT_TIME;
    }


    public ConcurrentMap<String, String> getDatabase() {
        return database;
    }

    public ConcurrentMap<String,
            ConcurrentMap<String, ConcurrentMap<String, String>>> getArchive() {
        return archive;
    }


    public void start() throws IOException {
        // Backup Pool Create Backup Snapshot
        schedulePool.scheduleWithFixedDelay(serverSnapshot::createSnapShot, BACKUP_TIME, BACKUP_TIME, TimeUnit.MINUTES);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            logger.info("Create a new client handling socket at " + clientSocket.getLocalSocketAddress());
            // Connection Pool listen for incoming requests
            connectionHandlerPool.execute(new ConnectionHandler(
                    clientSocket,
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream())),
                    new PrintWriter(clientSocket.getOutputStream(), true),
                    clock, database, archive, requestHandlerPool, updateQueue,
                    schedulePool, FRESH_PERIOD_COUNT, WAIT_TIME));
        }
    }

//    @IgnoreCoverage
//    public void close() throws IOException {
//        connectionHandlerPool.close();
//        schedulePool.close();
//        requestHandlerPool.close();
//        super.close();
//    }

}


