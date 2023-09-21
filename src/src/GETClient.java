public class GETClient extends SocketClient {
    private String stationID;

    public GETClient(String URL) {
        super(URL);
    }

    public GETClient(String URL, String stationID) {
        super(URL);
        this.stationID = stationID;
    }

    public String formatMessage() {
        String request = "GET /";
        if (stationID == null)
            request = request + " HTTP/1.1\r\n";
        else
            request = request + stationID + " HTTP/1.1\r\n";

        //Add Hostname
        request = request + "Host: " + getServerName() + ":" + getPort() + "\r\n";

        //Accept Json
        request = request + "Accept: application/json\r\n";
        return request;
    }

    public static void main(String[] argv) {
        GETClient client;
        System.out.println(argv.length);
        if (argv.length == 1) {
            System.out.println(argv[0]);
            client = new GETClient(argv[0]);
        }else if (argv.length == 2) {
            client = new GETClient(argv[0], argv[1]);
        } else {
            throw new RuntimeException("Usage GETClient URL [stationID]");
        }
        System.out.println(client.formatMessage());
    }
}
