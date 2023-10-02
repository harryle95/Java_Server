package handlers;

import annotations.IgnoreCoverage;
import utility.FileMetadata;
import utility.LamportClock;
import utility.SocketCommunicator;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.*;

public class ConnectionHandler extends SocketCommunicator implements Runnable {

    private final ConcurrentMap<String, String> database;

    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive;

    private final ExecutorService requestHandlerPool;

    private final LinkedBlockingQueue<FileMetadata> updateQueue;

    private final ScheduledExecutorService schedulePool;

    private final int FRESH_COUNT;
    private final int WAIT_TIME;

    public ConnectionHandler(
            Socket socket,
            BufferedReader in,
            PrintWriter out,
            LamportClock clock,
            ConcurrentMap<String, String> database,
            ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive,
            ExecutorService requestHandlerPool,
            LinkedBlockingQueue<FileMetadata> updateQueue,
            ScheduledExecutorService schedulePool, int freshcount, int waitTime) {
        super(socket, clock, out, in, "server");
        this.database = database;
        this.archive = archive;
        this.requestHandlerPool = requestHandlerPool;
        this.updateQueue = updateQueue;
        this.schedulePool = schedulePool;
        FRESH_COUNT = freshcount;
        this.WAIT_TIME = waitTime;
    }

    @IgnoreCoverage
    @Override
    public void run() {
        FileMetadata metadataPUT = null;
        try {
            String message;
            while (true) {
                message = receive();
                // Client will close the connection
                if (message == null)
                    break;
                HTTPRequest request = HTTPRequest.fromMessage(message);
                int receiveTS = clock.getTimeStamp();
                // Save metadata to remove archive's entry 30s after disconnection
                if (request.method.equals("PUT"))
                    metadataPUT = new FileMetadata(clientSocket.getInetAddress().toString(),
                            request.getURIEndPoint(), String.valueOf(receiveTS));

                // Submit request to a task queue and get the Future as a CompletionService
                logger.info("Submitting job to execution threadpool");
                Callable<HTTPResponse> task = new RequestHandler(
                        request,
                        clientSocket.getInetAddress().toString(),
                        receiveTS,
                        updateQueue,
                        database,
                        FRESH_COUNT,
                        archive
                );
                Future<HTTPResponse> future = requestHandlerPool.submit(task);
                HTTPResponse futureResponse = future.get();
                send(futureResponse);
            }
            // Submit a cleanup task if request is PUT
            if (metadataPUT != null) {
                logger.info("Schedule a job to remove entry: " + metadataPUT.getRemoteIP() + "/" + metadataPUT.getFileName() + " after " + WAIT_TIME);
                Runnable removeArchiveData = new RemoveEntryRunnable(metadataPUT, archive);
                schedulePool.schedule(removeArchiveData, WAIT_TIME, TimeUnit.MILLISECONDS);
            }
            close();
        } catch (IOException | ExecutionException | InterruptedException e) {
            logger.info("Runtime exception " + e.getMessage());
        }
    }
}

