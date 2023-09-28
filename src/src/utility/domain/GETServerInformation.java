package utility.domain;

public class GETServerInformation extends ServerInformation {
    public String stationID;

    public GETServerInformation(ServerInformation info) {
        super(info);
    }


    public void setStationID(String stationID) {
        this.stationID = stationID;
    }
}
