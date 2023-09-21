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

    public String build() {
        StringBuilder message = new StringBuilder();

        // Add request line
        message.append(String.format("HTTP/%s %s %s\r\n", version, statusCode, reasonPhrase));

        // Add headers
        if (!header.isEmpty()) {
            StringBuilder requestHeader = new StringBuilder();
            for (Map.Entry<String, String> entry : header.entrySet()) {
                requestHeader.append(String.format("%s: %s\r\n", entry.getKey(), entry.getValue()));
            }
            message.append(requestHeader);
        }
        message.append("\r\n");
        // Add body
        if (body != null) {
            message.append(body);
        }
        return message.toString();
    }

    public static HTTPResponse fromMessage(String message) {
        String[] components = message.split("\r\n");
        String responseLine = components[0];
        String statusCode, reasonPhrase, version, body;
        String[] responseLineComponents = responseLine.split(" ");
        version = responseLineComponents[0].split("/")[1];
        statusCode = responseLineComponents[1];

        // Get reason phrase
        int index = 2;
        StringBuilder reasonBuilder = new StringBuilder();
        for (index = 2; index < responseLineComponents.length; index++) {
            reasonBuilder.append(responseLineComponents[index]);
            reasonBuilder.append(" ");
        }
        reasonPhrase = reasonBuilder.toString().trim();
        // Get Header

        Map<String, String> header = new LinkedHashMap<>();
        index = 1;
        while (index < components.length && !components[index].isEmpty()) {
            String[] headerLine = components[index].split(": ");
            header.put(headerLine[0], headerLine[1]);
            index += 1;
        }

        // Get Body
        if (index == components.length | components[components.length - 1].isEmpty())
            body = null;
        else
            body = components[components.length - 1];
        return new HTTPResponse(version, statusCode, reasonPhrase, header, body);
    }
}
