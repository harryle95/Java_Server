import utility.LamportClock;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;
import utility.MessageExchanger;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AggregationServer {
    private ServerSocket serverSocket;
    private final ExecutorService connRecvPool; // Threadpool to accept incoming requests

    private final LamportClock clock;

    public int printTimestamp() {
        return clock.printTimestamp();
    }

    public AggregationServer(String[] argv) throws IOException {
        int port = getPort(argv);
        connRecvPool = Executors.newCachedThreadPool();
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
            RequestHandler handler = new RequestHandler(clientSocket, clock);
            connRecvPool.execute(handler);
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
    }

    public void close() throws IOException {
        serverSocket.close();
    }
}


class RequestHandler extends SocketCommunicator implements Runnable {

    public RequestHandler(Socket socket, LamportClock clock) throws IOException {
        super(socket, clock, "server");
    }

    private HTTPResponse handleGET(HTTPRequest request) {
        String body;
        if (request.uri.equals("/")) {
            body = "";
        } else {
            // TODO: check if StationID exists in Map
            body = request.uri.substring(1);
        }
        return new HTTPResponse("1.1")
                .setStatusCode("200")
                .setReasonPhrase("OK")
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private HTTPResponse handlePUT(HTTPRequest request) {
        String body = request.body;
        // TODO: perform correct PUT
        return new HTTPResponse("1.1")
                .setStatusCode("200")
                .setReasonPhrase("OK")
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }


    private HTTPResponse getResponse(HTTPRequest request) {
        HTTPResponse response;
        if (request.method.equals("GET"))
            response = handleGET(request);
        else if (request.method.equals("PUT"))
            response = handlePUT(request);
        else
            response = new HTTPResponse("1.1").setStatusCode("400").setReasonPhrase("Bad Request");
        return response;
    }

    @Override
    public void run() {
        try {
            String message;
            while (true) {
                System.out.println("Before receive: " + clock.printTimestamp());
                message = receive();
                if (message == null)
                    break;
//                System.out.println(message);
                System.out.println("After receive: " + clock.printTimestamp());
                HTTPRequest request = HTTPRequest.fromMessage(message);
                // TODO: submit request to a task queue and get the Future as a CompletionService

                send(getResponse(request));
                System.out.println("After send: " + clock.printTimestamp());

            }
            System.out.println("Socket is closed");
            clientSocket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}