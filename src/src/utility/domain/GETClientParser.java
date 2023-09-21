package utility.domain;

public class GETClientParser extends DomainParser {
    @Override
    public GETServerInformation parse(String[] argv) {
        if (argv.length == 1) {
            return GETServerInformation.fromServerInfo(parseURL(argv[0]));
        } else if (argv.length == 2) {
            GETServerInformation info =
                    GETServerInformation.fromServerInfo(parseURL(argv[0]));
            info.setStationID(argv[1]);
            return info;
        } else {
            throw new RuntimeException("Usage GETClient URL [stationID]");
        }
    }
}
