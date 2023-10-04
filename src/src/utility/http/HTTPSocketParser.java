package utility.http;

public class HTTPSocketParser {
    private HTTPMessage message;
    private int lineParsed;
    private boolean isBody;

    public boolean isComplete() {
        return isComplete;
    }

    private boolean isComplete;

    private int body_length;

    private StringBuilder body;

    public HTTPSocketParser() {
        reset();
    }

    public void reset() {
        message = null;
        lineParsed = 0;
        isBody = false;
        isComplete = false;
        body_length = 0;
        body = new StringBuilder();
    }

    public String toString() {
        return message.toString();
    }

    private String joinArray(String[] array, int index, String sep) {
        StringBuilder value = new StringBuilder();
        for (int i = index; i < array.length; i++) {
            value.append(array[i]);
            if (i < array.length - 1)
                value.append(sep);
        }
        return value.toString().trim();
    }

    private String removeCacheReturn(String line) {
        if (line.endsWith("\r")) {
            line = line.substring(0, line.length() - 1);
        }
        return line;
    }

    public void parseLine(String line) {
        if (lineParsed == 0) {
            line = removeCacheReturn(line);
            String[] terms = line.split(" ");
            if (terms[0].equals("GET") || terms[0].equals("PUT") || terms[0].equals("POST") ||
                    terms[0].equals("DELETE") || terms[0].equals("PATCH")) {
                message = new HTTPRequest(terms[2].substring(5)).setMethod(terms[0]).setURI(terms[1]);
            } else if (line.isEmpty()) { // Very hacky hack,
                lineParsed = lineParsed == 0 ? -1 : lineParsed;
            } else {
                message = new HTTPResponse(terms[0].substring(5)).setStatusCode(terms[1]).setReasonPhrase(joinArray(terms, 2, " "));
            }
        } else {
            if (!isBody) {
                line = removeCacheReturn(line);
                if (line.trim().isEmpty()) {
                    isBody = true;
                    String content_length = message.getHeader("Content-Length");
                    if (content_length == null) {
                        body_length = 0;
                        message.setBody(body.toString());
                        isComplete = true;
                    } else {
                        try {
                            body_length = Integer.parseInt(content_length);
                        } catch (RuntimeException e) {
                            body_length = 0;
                            message.setBody(body.toString());
                            isComplete = true;
                        }
                    }
                } else {
                    String[] headerKeyValue = line.split(":");
                    message.setHeader(headerKeyValue[0].trim(), joinArray(headerKeyValue, 1, ":"));
                }
            } else {
                body.append(line);
                body_length = Math.max((body_length - line.length()), 0);
                if (body_length > 0) {
                    body.append("\n");
                    body_length -= 1;
                }
                if (body_length == 0 && !isComplete) {
                    message.setBody(body.toString());
                    isComplete = true;
                }
            }
        }
        lineParsed += 1;
    }
}
