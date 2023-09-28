package utility.domain;

public abstract class DomainParser {

    /**
     * Parse URL component, return hostname, port info
     * <p>
     * URL must be of the following 3 forms:
     * hostname:port
     * http://hostname:port
     * http://hostname.domain:port
     *
     * @param URL url string
     * @return ServerInformation containing hostname and port info
     */
    protected ServerInformation parseURL(String URL) {
        String[] split_string = URL.split(":");
        if (split_string.length == 2) {
            return new ServerInformation(split_string[0],
                    Integer.parseInt(split_string[1]));
        } else if (split_string.length != 3) {
            throw new RuntimeException("URL must be server:port or http://server:port" +
                    " or http://server.domain:port");
        } else {
            String _serverName = split_string[1].split("\\.")[0];
            if (!_serverName.startsWith("//")) {
                throw new RuntimeException("URL must be server:port or " +
                        "http://server:port or http://server.domain:port");
            }
            return new ServerInformation(_serverName.substring(2),
                    Integer.parseInt(split_string[2]));
        }
    }

    public abstract ServerInformation parse(String[] argv);
}

