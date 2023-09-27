import utility.LamportClock;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

public class ConnectionHandler extends SocketCommunicator implements Runnable {

    private final ConcurrentMap<String, String> database;

    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive;

    private final ExecutorService requestHandlerPool;

    public ConnectionHandler(
            Socket socket,
            LamportClock clock,
            ConcurrentMap<String, String> database,
            ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive,
            ExecutorService requestHandlerPool) throws IOException {
        super(socket, clock, "server");
        this.database = database;
        this.archive = archive;
        this.requestHandlerPool = requestHandlerPool;
    }


    @Override
    public void run() {
        try {
            String message;
            while (true) {
                message = receive();
                if (message == null)
                    break;
                HTTPRequest request = HTTPRequest.fromMessage(message);
                // Submit request to a task queue and get the Future as a CompletionService
                Callable<HTTPResponse> task = new RequestHandler(
                        request,
                        clientSocket.getRemoteSocketAddress().toString(),
                        clock,
                        database,
                        archive
                );
                Future<HTTPResponse> future = requestHandlerPool.submit(task);
                send(future.get());
            }
            System.out.println("Closing socket");
            clientSocket.close();
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
