package utility.domain;

public class GETServerInformation extends ServerInformation {
    public String stationID;

    public GETServerInformation(String hostname, int port) {
        super(hostname, port);
    }

    public static GETServerInformation fromServerInfo(ServerInformation item) {
        return new GETServerInformation(item.hostname, item.port);
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }
}
