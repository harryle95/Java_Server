package utility.http;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MessageExchanger {
    public static String encode(String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String decode(String message) {
        byte[] bytes = Base64.getDecoder().decode(message);
        return new String(bytes);
    }
}
