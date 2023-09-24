import utility.domain.GETClientParser;
import utility.domain.GETServerInformation;
import utility.http.HTTPRequest;
import utility.http.MessageExchanger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GETClient extends SocketClient {
    private String stationID;

    public GETClient(String[] argv) {
        GETClientParser parser = new GETClientParser();
        GETServerInformation info = parser.parse(argv);
        setHostname(info.hostname);
        setPort(info.port);
        setStationID(info.stationID);
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public HTTPRequest formatMessage() {
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
            connect();
            HTTPRequest request = formatMessage();
            System.out.println(request.build());
            out.println(request.build());
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    public static void main(String[] argv) throws IOException {
        GETClient client = new GETClient(argv);
        client.connect();
        HTTPRequest request = client.formatMessage();
        System.out.println(request.build());
        client.out.println(MessageExchanger.encode(request.build()));
        while (true) {
            String output = client.in.readLine();
            System.out.println(MessageExchanger.decode(output));
            break;
        }
    }
}
