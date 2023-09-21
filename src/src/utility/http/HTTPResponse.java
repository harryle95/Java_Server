package utility.http;

import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPResponse {
    public String version;
    public String statusCode;
    public String reasonPhrase;
    public Map<String, String> header;
    public String body;

    public HTTPResponse(String version) {
        this.version = version;
        this.header = new LinkedHashMap<>();
    }

    public HTTPResponse(
            String version,
            String statusCode,
            String reasonPhrase,
            Map<String, String> header,
            String body) {
        this.version = version;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.header = header;
        this.body = body;
    }

    public HTTPResponse setStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public HTTPResponse setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
        return this;
    }

    public HTTPResponse setHeader(String key, String value) {
        this.header.put(key, value);
        return this;
    }

    public HTTPResponse setBody(String body) {
        this.body = body;
        return this;
    }

    public static HTTPResponse fromMessage(String message) {
        String[] components = message.split("\r\n");
        String responseLine = components[0];
        String statusCode, reasonPhrase, version, body;
        String[] responseLineComponents = responseLine.split(" ");
        version = responseLineComponents[0].split("/")[1];
        statusCode = responseLineComponents[1];
        reasonPhrase = responseLineComponents[2];
        Map<String, String> header = new LinkedHashMap<>();
        int index = 1;
        while (index < components.length && !components[index].isEmpty()) {
            String[] headerLine = components[index].split(": ");
            header.put(headerLine[0], headerLine[1]);
            index += 1;
        }
        if (index == components.length | components[components.length - 1].isEmpty())
            body = null;
        else
            body = components[components.length - 1];
        return new HTTPResponse(version, statusCode, reasonPhrase, header, body);
    }
}
