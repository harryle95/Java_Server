package utility.domain;

public class ServerInformation {
    public final String hostname;
    public final int port;

    public ServerInformation(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
}
