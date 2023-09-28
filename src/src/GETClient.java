import utility.SocketClient;
import utility.domain.GETClientParser;
import utility.domain.GETServerInformation;
import utility.http.HTTPRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GETClient extends SocketClient {
    public String getStationID() {
        return stationID;
    }

    private final String stationID;

    public GETClient(
            Socket clientSocket,
            PrintWriter out,
            BufferedReader in,
            String hostname,
            int port,
            String stationID){
        super(clientSocket, out, in);
        this.hostname = hostname;
        this.port = port;
        this.stationID = stationID;
    }


    public static GETClient from_args(String[] argv) throws IOException {
        GETClientParser parser = new GETClientParser();
        GETServerInformation info = parser.parse(argv);
        Socket clientSocket = new Socket(info.hostname, info.port);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        return new GETClient(clientSocket, out, in, info.hostname, info.port, info.stationID);
    }


    public HTTPRequest formatGETMessage() {
        HTTPRequest request = new HTTPRequest("1.1").setMethod("GET");

        if (stationID == null)
            request.setURI("/");
        else
            request.setURI("/" + stationID);

        //Add Hostname
        request.setHeader("Host", getHostname() + ":" + getPort());

        //Accept Json
        request.setHeader("Accept", "application/json");
        return request;
    }

    public void run() {
        try {
            HTTPRequest request = formatGETMessage();
            send(request);
            while (true) {
                String response = receive();
                if (response != null)
                    System.out.println(response);
                break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    public static void main(String[] argv) throws IOException {
        GETClient client = GETClient.from_args(argv);
        client.run();
    }
}
