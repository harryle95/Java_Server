import utility.domain.ContentServerInformation;
import utility.domain.ContentServerParser;
import utility.http.HTTPRequest;
import utility.json.Parser;

import java.io.IOException;
import java.nio.file.Path;

public class ContentServer extends SocketClient {
    private String fileName;


    public ContentServer(String[] argv) {
        ContentServerParser parser = new ContentServerParser();
        ContentServerInformation info = parser.parse(argv);
        setHostname(info.hostname);
        setPort(info.port);
        setFileName(info.fileName);
    }

    private String getBody() throws IOException {
        Parser parser = new Parser();
        parser.parseFile(Path.of(fileName));
        return parser.toString();
    }

    public HTTPRequest formatMessage() throws IOException {
        HTTPRequest request = new HTTPRequest("1.1")
                .setMethod("PUT")
                .setURI("/" + fileName)
                .setHeader("Host", getHostname() + ":" + getPort())
                .setHeader("Accept", "application/json")
                .setHeader("Content-Type", "application/json");
        String body = getBody();
        request.setHeader("Content-Length", String.valueOf(body.length()));
        request.setBody(body);
        return request;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
