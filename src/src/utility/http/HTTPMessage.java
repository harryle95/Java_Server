package utility.http;

import java.util.Map;

public class HTTPMessage {
    public final String method;
    public final Map<String, String> header;
    public final String body;

    public HTTPMessage(String method, Map<String, String> header) {
        this.method = method;
        this.header = header;
        this.body = null;
    }

    public HTTPMessage(String method, Map<String, String> header, String body) {
        this.method = method;
        this.header = header;
        this.body = body;
    }

}
