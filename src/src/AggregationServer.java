import annotations.IgnoreCoverage;
import handlers.ConnectionHandler;
import handlers.PriorityRunnableFuture;
import handlers.PriorityRunnableFutureComparator;
import handlers.RequestHandler;
import utility.FileMetadata;
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
    private final ServerSnapshot serverSnapshot; // Server snapshot service
    // incoming requests
    private final int POOL_SIZE = Integer.parseInt(config.get("POOL_SIZE", "20"));
    // how long to wait until the cleanup task - 30 seconds
    private final int BACKUP_TIME =
            Integer.parseInt(config.get("BACKUP_TIME", "15")); // time between auto
    ScheduledFuture<?> createSnapShotFuture;

    ScheduledFuture<?> removeEntryFuture;
    private int FRESH_PERIOD_COUNT = Integer.parseInt(config.get("FRESH_PERIOD_COUNT"
            , "20")); // how many updates until the current is no longer fresh
    private int WAIT_TIME = Integer.parseInt(config.get("WAIT_TIME", "30000")); //

    public AggregationServer(int port) throws IOException, ClassNotFoundException {
        super(port);
        serverSnapshot = new ServerSnapshot(
                config.get("databaseDir", "src/backups/database"),
                config.get("archiveDir", "src/backups/archive"));
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
        run();
    }
    // backup - default 15 minutes

    @IgnoreCoverage
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int port = getPort(args);
        AggregationServer server = new AggregationServer(port);
        server.start();
        server.close();
    }

    /**
     * Getter for serverSnapshot
     *
     * @return serverSnapshot object
     */
    public ServerSnapshot getServerSnapshot() {
        return serverSnapshot;
    }

    /**
     * Setter for FRESH_PERIOD_COUNT only for testing
     *
     * @param FRESH_PERIOD_COUNT int value
     */
    public void setFRESH_PERIOD_COUNT(int FRESH_PERIOD_COUNT) {
        this.FRESH_PERIOD_COUNT = FRESH_PERIOD_COUNT;
    }

    /**
     * Setter for WAIT_TIME only for testing
     *
     * @param WAIT_TIME int value
     */
    public void setWAIT_TIME(int WAIT_TIME) {
        this.WAIT_TIME = WAIT_TIME;
    }


    /**
     * Getter for database
     *
     * @return database object
     */
    public ConcurrentMap<String, String> getDatabase() {
        return database;
    }

    /**
     * Getter for archive
     *
     * @return archive object
     */
    public ConcurrentMap<String,
            ConcurrentMap<String, ConcurrentMap<String, String>>> getArchive() {
        return archive;
    }


    /**
     * Create a backup task that runs every 15 minutes or BACKUP_TIME
     */
    @Override
    protected void pre_start_hook() {
        super.pre_start_hook();
        createSnapShotFuture =
                schedulePool.scheduleWithFixedDelay(serverSnapshot::createSnapShot,
                        BACKUP_TIME, BACKUP_TIME, TimeUnit.MINUTES);
    }

    /**
     * When a new connection request is made, a thread from connectionHandlerPool is set to handle connection tasks
     */
    @Override
    protected void start_hook() {
        super.start_hook();
        try {
            Socket clientSocket = serverSocket.accept();
            logger.info("Create a new client handling socket at " + clientSocket.getLocalSocketAddress());
            // Connection Pool listen for incoming requests
            connectionHandlerPool.execute(new ConnectionHandler(
                    clientSocket,
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream())),
                    new PrintWriter(clientSocket.getOutputStream(), true),
                    clock, database, archive, requestHandlerPool, updateQueue,
                    schedulePool, FRESH_PERIOD_COUNT, WAIT_TIME, removeEntryFuture));
        } catch (IOException e) {
            logger.info("ERROR: start_hook for AggregationServer: " + e);
            setStartBreakSignal(true);
        }
    }

    /**
     * Stop all thread pools and future tasks
     */
    @Override
    protected void pre_close_hook() {
        super.pre_close_hook();
        if (createSnapShotFuture != null)
            createSnapShotFuture.cancel(true);
        if (removeEntryFuture != null)
            removeEntryFuture.cancel(true);
        logger.info("Closing agg server connection handler pool: " + connectionHandlerPool.isTerminated());
        connectionHandlerPool.shutdownNow();
        logger.info("Closing agg server schedule pool");
        schedulePool.shutdownNow();
        logger.info("Closing agg server request pool");
        requestHandlerPool.shutdownNow();
    }
}


