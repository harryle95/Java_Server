import utility.domain.GETClientParser;
import utility.domain.GETServerInformation;
import utility.http.HTTPRequest;

public class GETClient extends SocketClient {
    private String stationID;

    public GETClient(String[] argv) {
        super(argv);
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

    public String formatMessage() {
        HTTPRequest request = new HTTPRequest("1.1").setMethod("GET");

        if (stationID == null)
            request.setURI("/");
        else
            request.setURI("/" + stationID);

        //Add Hostname
        request.setHeader("Host", getHostname()+":"+getPort());

        //Accept Json
        request.setHeader("Accept", "application/json");
        return request.build();
    }
}
