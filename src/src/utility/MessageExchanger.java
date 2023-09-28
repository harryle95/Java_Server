package utility;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MessageExchanger {
    /**
     * Encode UTF8. Useful for sending and receiving whole message
     *
     * @param message un-encoded message
     * @return message encoded UTF8
     */
    public static String encode(String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decode UTF8 messages. Used for receiving encoded message
     *
     * @param message encoded message
     * @return decoded message
     */
    public static String decode(String message) {
        byte[] bytes = Base64.getDecoder().decode(message);
        return new String(bytes);
    }
}
