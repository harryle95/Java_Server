package utility.domain;

public class ContentServerParser extends DomainParser {
    /**
     * Parse ContentServer argv and return hostname, port, fileName
     *
     * @param argv CLI argv
     * @return ContentServerInformation object containing hostname, port, fileName
     */
    @Override
    public ContentServerInformation parse(String[] argv) {
        if (argv.length == 2) {
            ContentServerInformation info =
                    new ContentServerInformation(parseURL(argv[0]));
            info.setFileName(argv[1]);
            return info;
        } else {
            throw new RuntimeException("Usage ContentServer URL fileName");
        }
    }
}
