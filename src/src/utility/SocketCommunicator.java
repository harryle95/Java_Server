package utility;

import utility.http.HTTPMessage;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class SocketCommunicator {
    public List<String> sentMessages;
    public List<String> receivedMessages;
    protected Socket clientSocket;
    protected LamportClock clock;
    PrintWriter out;
    BufferedReader in;
    String type;

    public boolean isUp;

    public SocketCommunicator(
            Socket clientSocket,
            LamportClock clock,
            PrintWriter out,
            BufferedReader in,
            String type) {
        isUp = true;
        this.clientSocket = clientSocket;
        this.clock = clock;
        this.in = in;
        this.out = out;
        this.type = type;
        sentMessages = new ArrayList<>();
        receivedMessages = new ArrayList<>();
    }

    public String receive() throws IOException {
        String encodedResponse = in.readLine();
        if (encodedResponse != null) {
            if (type.equals("client")) {
                HTTPResponse response =
                        HTTPResponse.fromMessage(MessageExchanger.decode(encodedResponse));
                clock.advanceAndSetTimeStamp(Integer.parseInt(response.header.get(
                        "Lamport-Clock")));
                receivedMessages.add(response.toString());
                return response.toString();
            } else {
                HTTPRequest request =
                        HTTPRequest.fromMessage(MessageExchanger.decode(encodedResponse));
                clock.advanceAndSetTimeStamp(Integer.parseInt(request.header.get(
                        "Lamport-Clock")));
                receivedMessages.add(request.toString());
                return request.toString();
            }
        }
        return null;
    }


    public void send(HTTPMessage message) {
        int TS = clock.advanceAndGetTimeStamp();
        message.setHeader("Lamport-Clock", String.valueOf(TS));
        sentMessages.add(message.toString());
        out.println(MessageExchanger.encode(message.toString()));
    }


    public void close() throws IOException {
        clientSocket.close();
        System.out.println("Closing client-side connection");
        isUp = false;
    }
}
