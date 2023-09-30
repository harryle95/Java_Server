import Coverage.IgnoreCoverage;
import handlers.ConnectionHandler;
import handlers.PriorityRunnableFuture;
import handlers.PriorityRunnableFutureComparator;
import handlers.RequestHandler;
import utility.FileMetadata;
import utility.LamportClock;
import utility.ServerSnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

//TODO - Add HeartBeat/HealthCheck Runnable
public class AggregationServer {
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
    public boolean isUp;
    private int FRESH_PERIOD_COUNT = 20; // how many updates until the current is no longer fresh
    private int WAIT_TIME = 30000; // how long to wait until the cleanup task - 30 seconds
    private final int BACKUP_TIME = 15; // time between auto backup - default 15 minutes
    private ServerSocket serverSocket;

    public AggregationServer(String[] argv) throws IOException, ClassNotFoundException {
        int port = getPort(argv);
        isUp = true;
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
        run(port);
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

    @IgnoreCoverage
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        AggregationServer server = new AggregationServer(args);
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

    public void run(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
//        System.out.println("Server listening at port: " + port);
    }

    public void start() throws IOException {
        // Backup Pool Create Backup Snapshot
        schedulePool.scheduleWithFixedDelay(()->{
            try {
                serverSnapshot.createSnapShot();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, BACKUP_TIME, BACKUP_TIME, TimeUnit.MINUTES);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            // Connection Pool listen for incoming requests
            connectionHandlerPool.execute(new ConnectionHandler(
                    clientSocket,
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream())),
                    new PrintWriter(clientSocket.getOutputStream(), true),
                    clock, database, archive, requestHandlerPool, updateQueue,
                    schedulePool, FRESH_PERIOD_COUNT, WAIT_TIME));
        }
    }

    public void close() throws IOException {
        serverSocket.close();
        isUp = false;
    }
}


