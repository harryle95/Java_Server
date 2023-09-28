package utility.domain;

/**
 * ServerID including hostname and port
 */
public class ServerInformation {
    public final String hostname;
    public final int port;

    /**
     * Constructor for ServerInformation
     *
     * @param hostname name of server
     * @param port     connecting port
     */
    public ServerInformation(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Construct ServerInformation from another (Copy constructor)
     *
     * @param other another ServerInformation object
     */
    public ServerInformation(ServerInformation other) {
        this.hostname = other.hostname;
        this.port = other.port;
    }
}
