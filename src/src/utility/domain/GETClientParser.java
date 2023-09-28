package utility.domain;

public class GETClientParser extends DomainParser {
    /**
     * Parse CLIENT argv and return hostname, port, stationID
     *
     * @param argv CLI argv
     * @return GETServerInformation object containing hostname, port, stationID
     */
    @Override
    public GETServerInformation parse(String[] argv) {
        GETServerInformation result = new GETServerInformation(parseURL(argv[0]));
        if (argv.length == 1) {
            return result;
        } else if (argv.length == 2) {
            result.setStationID(argv[1]);
            return result;
        } else {
            throw new RuntimeException("Usage GETClient URL [stationID]");
        }
    }
}
