package utility.http;

import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPRequest {
    public String method;
    public String uri;
    public String version;
    public Map<String, String> header;
    public String body;

    public HTTPRequest(String version) {
        this.version = version;
        this.header = new LinkedHashMap<>();
    }

    public HTTPRequest(String method, String uri, String version, Map<String, String> header, String body) {
        this.method = method;
        this.uri = uri;
        this.version = version;
        this.header = header;
        this.body = body;
    }

    public HTTPRequest setMethod(String method) {
        this.method = method.toUpperCase();
        return this;
    }

    public HTTPRequest setURI(String uri) {
        this.uri = uri;
        return this;
    }

    public HTTPRequest setHeader(String key, String value) {
        this.header.put(key, value);
        return this;
    }

    public HTTPRequest setBody(String body) {
        this.body = body;
        return this;
    }

    public String build() {
        StringBuilder message = new StringBuilder();

        // Add request line
        message.append(String.format("%s %s HTTP/%s\r\n", method, uri, version));

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

    public static HTTPRequest fromMessage(String message) {
        String[] components = message.split("\r\n");
        String requestLine = components[0];
        String method, uri, version, body;
        String[] requestLineComponents = requestLine.split(" ");
        method = requestLineComponents[0];
        uri = requestLineComponents[1];
        version = requestLineComponents[2].split("/")[1];
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
        return new HTTPRequest(method, uri, version, header, body);
    }

}
