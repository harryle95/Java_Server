import utility.domain.GETClientParser;
import utility.domain.GETServerInformation;
import utility.http.HTTPRequest;

import java.io.IOException;

public class GETClient extends SocketClient {
    private String stationID;

    public GETClient(String[] argv) {
        super();
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

    public static void main(String[] argv) {
        GETClient client = new GETClient(argv);
        client.run();
    }
}
