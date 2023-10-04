package utility;

import utility.config.Config;
import utility.http.HTTPMessage;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;
import utility.http.HTTPSocketParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class SocketCommunicator {
    public Logger logger;
    public List<String> sentMessages;
    public List<String> receivedMessages;
    protected Socket clientSocket;
    protected LamportClock clock;
    PrintWriter out;
    BufferedReader in;
    String type;

    protected Config config = new Config("src/config/client.properties");

    public boolean isUp;

    public SocketCommunicator(
            Socket clientSocket,
            LamportClock clock,
            PrintWriter out,
            BufferedReader in,
            String type) {
        this.logger = Logger.getLogger(this.getClass().getName());
        isUp = true;
        this.clientSocket = clientSocket;
        this.clock = clock;
        this.in = in;
        this.out = out;
        this.type = type;
        sentMessages = new ArrayList<>();
        receivedMessages = new ArrayList<>();
        logger.info("Connecting to remote: " + clientSocket.getRemoteSocketAddress());
    }

    private String receiveMessage() throws IOException {
        HTTPSocketParser socketParser = new HTTPSocketParser();

        while (true) {
            String line = in.readLine();
            if (line != null) {
                socketParser.parseLine(line);
                if (socketParser.isComplete())
                    return socketParser.toString();
            } else {
                return null;
            }
        }
    }

    private int parseLamportClock(HTTPMessage message) {
        String ts = message.getHeader("Lamport-Clock");
        int value = 0;
        if (ts != null) {
            try {
                value = Integer.parseInt(ts);
            } catch (RuntimeException e) {
                value = 0;
            }
        }
        return value;
    }

    public String receive() throws IOException {
        String receivedResponse = receiveMessage();
        if (receivedResponse != null) {
            if (type.equals("client")) {
                HTTPResponse response =
                        HTTPResponse.fromMessage(receivedResponse);
                clock.advanceAndSetTimeStamp(parseLamportClock(response));
                receivedMessages.add(response.toString());
                logger.info("Receive response at " + this.getClass().getName() + ":\n" + response);
                return response.toString();
            } else {
                HTTPRequest request =
                        HTTPRequest.fromMessage(receivedResponse);
                clock.advanceAndSetTimeStamp(parseLamportClock(request));
                receivedMessages.add(request.toString());
                logger.info("Receive request at " + this.getClass().getName() + ":\n" + request);
                return request.toString();
            }
        }
        logger.info("Receive null");
        return null;
    }


    public void send(HTTPMessage message) {
        int TS = clock.advanceAndGetTimeStamp();
        message.setHeader("Lamport-Clock", String.valueOf(TS));
        logger.info("Sending message from " + this.getClass().getName() + ": \n" + message);
        sentMessages.add(message.toString());
        out.println(message);
    }


    public void close() throws IOException {
        logger.info("Closing " + type + " connection");
        clientSocket.close();
        isUp = false;
    }
}
