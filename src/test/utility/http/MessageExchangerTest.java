package utility.http;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import utility.MessageExchanger;

import static org.junit.jupiter.api.Assertions.*;

class MessageExchangerTest {

    @ParameterizedTest
    @ValueSource(strings={
            "GET /data HTTP/1.1\r\nHost: localhost:8080\r\nAccept: application/json\r\n\r\n",
            "GET / HTTP/1.1\r\nHost: localhost:8080\r\n\r\n",
            "GET localhost:8080/data HTTP/1.1\r\n\r\n",
            "PUT localhost:8080/data HTTP/1.1\r\nAccept: application/json\r\n\r\n{Animal: Dog}",
            "PUT localhost:8080/data HTTP/1.1\r\nAccept: application/json\r\nContent-Length: 12\r\n\r\n{Animal: Dog}",
            "PUT /data HTTP/1.1\r\nHost: localhost:8080\r\nAccept: application/json\r\nContent-Length: 12\r\n\r\n{Animal: Dog}",
            "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"id\": \"dog\"}",
            "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n",
            "HTTP/1.1 201 Created\r\nContent-Type: application/json\r\n\r\n{\"id\": \"dog\"}",
            "HTTP/1.1 201 Created\r\nContent-Type: application/json\r\n\r\n",
            "HTTP/1.1 204 No Content\r\nContent-Type: application/json\r\n\r\n",
            "HTTP/1.1 400 Bad Request\r\nContent-Type: application/json\r\n\r\n",
            "HTTP/1.1 500 Internal Server Error\r\nContent-Type: application/json\r\n\r\n"
    })
    void encodeDecode(String message) {
        String encoded = MessageExchanger.encode(message);
        assertEquals(message, MessageExchanger.decode(encoded));
    }
}