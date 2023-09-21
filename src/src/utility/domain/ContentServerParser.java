package utility.domain;

public class ContentServerParser extends DomainParser {
    @Override
    public ContentServerInformation parse(String[] argv) {
        if (argv.length == 2) {
            ContentServerInformation info =
                    ContentServerInformation.fromServerInfo(parseURL(argv[0]));
            info.setFileName(argv[1]);
            return info;
        } else {
            throw new RuntimeException("Usage ContentServer URL fileName");
        }
    }
}
