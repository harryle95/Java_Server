package handlers;

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

    private final int freshUpdateCount = 20;
    private final int waitTime = 30;

    public ConnectionHandler(
            Socket socket,
            BufferedReader in,
            PrintWriter out,
            LamportClock clock,
            ConcurrentMap<String, String> database,
            ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive,
            ExecutorService requestHandlerPool,
            LinkedBlockingQueue<FileMetadata> updateQueue,
            ScheduledExecutorService schedulePool) {
        super(socket, clock, out, in, "server");
        this.database = database;
        this.archive = archive;
        this.requestHandlerPool = requestHandlerPool;
        this.updateQueue = updateQueue;
        this.schedulePool = schedulePool;
    }


    @Override
    public void run() {
        FileMetadata metadataPUT = null;
        try {
            String message;
            while (true) {
                message = receive();
                if (message == null)
                    break;
                HTTPRequest request = HTTPRequest.fromMessage(message);
                // Save metadata to remove archive's entry 30s after disconnection
                if (request.method.equals("PUT"))
                    metadataPUT = new FileMetadata(clientSocket.getRemoteSocketAddress().toString(),
                            request.getURIEndPoint(), String.valueOf(clock.getTimeStamp()));

                // Submit request to a task queue and get the Future as a CompletionService
                Callable<HTTPResponse> task = new RequestHandler(
                        request,
                        clientSocket.getRemoteSocketAddress().toString(),
                        clock.getTimeStamp(),
                        updateQueue,
                        database,
                        freshUpdateCount,
                        archive
                );
                Future<HTTPResponse> future = requestHandlerPool.submit(task);
                send(future.get());
            }
            // Submit a cleanup task if request is PUT
            if (metadataPUT != null) {
                Runnable removeArchiveData = new RemoveEntryRunnable(metadataPUT, archive);
                schedulePool.schedule(removeArchiveData, waitTime, TimeUnit.SECONDS);
            }
            System.out.println("Closing socket");
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

