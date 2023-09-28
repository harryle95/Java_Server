package utility;
import utility.http.HTTPMessage;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class SocketCommunicator {
    protected Socket clientSocket;
    PrintWriter out;
    BufferedReader in;

    protected LamportClock clock;
    String type;

    public SocketCommunicator(
            Socket clientSocket,
            LamportClock clock,
            PrintWriter out,
            BufferedReader in,
            String type) {
        this.clientSocket = clientSocket;
        this.clock = clock;
        this.in = in;
        this.out = out;
        this.type = type;
    }

    public String receive() throws IOException {
        String encodedResponse = in.readLine();
        if (encodedResponse != null) {
            if (type.equals("client")) {
                HTTPResponse response = HTTPResponse.fromMessage(MessageExchanger.decode(encodedResponse));
                clock.advanceAndSetTimeStamp(Integer.parseInt(response.header.get("Lamport-Clock")));
                return response.toString();
            } else {
                HTTPRequest request = HTTPRequest.fromMessage(MessageExchanger.decode(encodedResponse));
                clock.advanceAndSetTimeStamp(Integer.parseInt(request.header.get("Lamport-Clock")));
                return request.toString();
            }
        }
        return null;
    }


    public void send(HTTPMessage message) {
        int TS = clock.advanceAndGetTimeStamp();
        message.setHeader("Lamport-Clock", String.valueOf(TS));
        out.println(MessageExchanger.encode(message.toString()));
    }


    public void close() {
        try {
            clientSocket.close();
            System.out.println("Closing connection");
        } catch (IOException e) {
            System.out.println("Socket already closed");
        }
    }
}