package utility.http;

import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPResponse implements HTTPMessage {
    public String version;
    public String statusCode;
    public String reasonPhrase;
    public Map<String, String> header;
    public String body;

    /**
     * HTTPResponse builder
     *
     * @param version HTTP version
     */
    public HTTPResponse(String version) {
        this.version = version;
        this.header = new LinkedHashMap<>();
    }

    /**
     * HTTPResponse builder
     *
     * @param version      HTTP version
     * @param statusCode   HTTP response status code
     * @param reasonPhrase HTTP response reasonPhrase
     * @param header       HTTP response header
     * @param body         HTTP response body
     */
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

    /**
     * Set status code in response message
     *
     * @param statusCode status code value
     * @return this HTTPResponse for method chaining
     */
    public HTTPResponse setStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Set reasons phrase in response message
     *
     * @param reasonPhrase reason phrase value
     * @return this HTTPResponse for method chaining
     */
    public HTTPResponse setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
        return this;
    }

    /**
     * Set HTTP response header
     *
     * @param key   header key
     * @param value header value
     * @return this HTTPResponse for method chaining
     */
    public HTTPResponse setHeader(String key, String value) {
        this.header.put(key, value);
        return this;
    }

    /**
     * Set HTTP response body
     *
     * @param body body message
     * @return this HTTPResponse for method chaining
     */
    public HTTPResponse setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public String getHeader(String key) {
        return this.header.get(key);
    }

    /**
     * Generate HTTPResponse message string
     *
     * @return HTTPResponse message string
     */
    public String toString() {
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

    /**
     * Generate HTTPResponse object based on a received message
     *
     * @param message socket received message
     * @return HTTPResponse object from message
     */
    public static HTTPResponse fromMessage(String message) {
        int index;
        String[] components = message.split("\r\n");
        String responseLine = components[0];
        String statusCode, reasonPhrase, version, body;
        String[] responseLineComponents = responseLine.split(" ");
        version = responseLineComponents[0].split("/")[1];
        statusCode = responseLineComponents[1];

        // Get reason phrase

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
