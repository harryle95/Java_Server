package utility.http;

public interface HTTPMessage {

    /**
     * Set message header
     *
     * @param key   header key
     * @param value header value
     * @return HTTPMessage message
     */
    HTTPMessage setHeader(String key, String value);


    /**
     * Generate HTTPMessage string based on builder
     *
     * @return HTTPMessage as string
     */
    String toString();
}
