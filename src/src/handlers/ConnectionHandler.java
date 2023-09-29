package handlers;

import Coverage.IgnoreCoverage;
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

    private final int FRESHCOUNT;
    private final int WAITTIME;

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
        FRESHCOUNT = freshcount;
        this.WAITTIME = waitTime;
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
                System.out.println(message);
                HTTPRequest request = HTTPRequest.fromMessage(message);
                // Save metadata to remove archive's entry 30s after disconnection
                if (request.method.equals("PUT"))
                    metadataPUT = new FileMetadata(clientSocket.getInetAddress().toString(),
                            request.getURIEndPoint(), String.valueOf(clock.getTimeStamp()));

                // Submit request to a task queue and get the Future as a CompletionService
                Callable<HTTPResponse> task = new RequestHandler(
                        request,
                        clientSocket.getInetAddress().toString(),
                        clock.getTimeStamp(),
                        updateQueue,
                        database,
                        FRESHCOUNT,
                        archive
                );
                Future<HTTPResponse> future = requestHandlerPool.submit(task);
                HTTPResponse futureResponse = future.get();
                System.out.println(futureResponse);
                send(futureResponse);
            }
            // Submit a cleanup task if request is PUT
            if (metadataPUT != null) {
                Runnable removeArchiveData = new RemoveEntryRunnable(metadataPUT, archive);
                schedulePool.schedule(removeArchiveData, WAITTIME, TimeUnit.SECONDS);
            }
            System.out.println("Closing server-side connection");
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

