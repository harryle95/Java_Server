import utility.http.HTTPRequest;
import utility.http.HTTPResponse;
import utility.MessageExchanger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AggregationServer {
    private ServerSocket serverSocket;
    private final ExecutorService connRecvPool; // Threadpool to accept incoming requests

    public AggregationServer() {
        connRecvPool = Executors.newCachedThreadPool();
    }

    public void run(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        System.out.println("Server listening at port: " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                RequestHandler handler = new RequestHandler(clientSocket);
                connRecvPool.execute(handler);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    public static void main(String[] args) {
        int port = getPort(args);
        try {
            AggregationServer server = new AggregationServer();
            server.run(port);
            server.start();
        } catch (BindException e) {
            System.out.println("Port already in use: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;

    // TODO: shared Lamport-Clock object to update timing
    public RequestHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("Connecting to: " + clientSocket.getRemoteSocketAddress());
    }

    private String handleGET(HTTPRequest request) {
        String body;
        if (request.uri.equals("/")) {
            body = "";
        } else {
            // TODO: check if StationID exists in Map
            body = request.uri.substring(1);
        }
        HTTPResponse response = new HTTPResponse("1.1")
                .setStatusCode("200")
                .setReasonPhrase("OK")
                .setHeader("Content-Type", "application/json")
                .setBody(body);
        return response.build();
    }

    private String handlePUT(HTTPRequest request) {
        String body = request.body;
        // TODO: perform correct PUT
        HTTPResponse response = new HTTPResponse("1.1")
                .setStatusCode("200")
                .setReasonPhrase("OK")
                .setHeader("Content-Type", "application/json")
                .setBody(body);
        return response.build();
    }


    private String getResponse(HTTPRequest request) {
        if (request.method.equals("GET"))
            return handleGET(request);
        if (request.method.equals("PUT"))
            return handlePUT(request);
        return new HTTPResponse("1.1").setStatusCode("400").setReasonPhrase("Bad Request").build();
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                HTTPRequest request = HTTPRequest.fromMessage(MessageExchanger.decode(message));
                // TODO: submit request to a task queue and get the Future as a CompletionService
                System.out.println(request.build());
                String response = getResponse(request);
                out.println(MessageExchanger.encode(response));
            }
            System.out.println("Socket is closed");
            clientSocket.close();
        } catch (SocketException e) {
            System.out.println("Connection aborted. Client is disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}