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
     * Set message body
     *
     * @param body message body
     * @return HTTPMessage message
     */
    HTTPMessage setBody(String body);


    /**
     * Get header value
     * @param key header key
     * @return header value if exists otherwise null
     */
    String getHeader(String key);



    /**
     * Generate HTTPMessage string based on builder
     *
     * @return HTTPMessage as string
     */
    String toString();
}
