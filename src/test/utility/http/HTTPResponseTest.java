package utility.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class HTTPResponseTest {

    @Test
    void buildResponse200() {
        String message = new HTTPResponse("1.1").
                setStatusCode("200").
                setReasonPhrase("OK").
                setHeader("Content-Type", "application/json").
                setBody("{\"id\": \"dog\"}").
                toString();
        assertEquals("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"id\": \"dog\"}", message);
    }

    @Test
    void buildResponse200NoBody() {
        String message = new HTTPResponse("1.1").
                setStatusCode("200").
                setReasonPhrase("OK").
                setHeader("Content-Type", "application/json").
                toString();
        assertEquals("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n", message);
    }

    @Test
    void buildResponse201() {
        String message = new HTTPResponse("1.1").
                setStatusCode("201").
                setReasonPhrase("Created").
                setHeader("Content-Type", "application/json").
                setBody("{\"id\": \"dog\"}").
                toString();
        assertEquals("HTTP/1.1 201 Created\r\nContent-Type: application/json\r\n\r\n{\"id\": \"dog\"}", message);
    }

    @Test
    void buildResponse201Nobody() {
        String message = new HTTPResponse("1.1").
                setStatusCode("201").
                setReasonPhrase("Created").
                setHeader("Content-Type", "application/json").
                toString();
        assertEquals("HTTP/1.1 201 Created\r\nContent-Type: application/json\r\n\r\n", message);
    }

    @Test
    void buildResponse204() {
        String message = new HTTPResponse("1.1").
                setStatusCode("204").
                setReasonPhrase("No Content").
                setHeader("Content-Type", "application/json").
                toString();
        assertEquals("HTTP/1.1 204 No Content\r\nContent-Type: application/json\r\n\r\n", message);
    }

    @Test
    void buildResponse400() {
        String message = new HTTPResponse("1.1").
                setStatusCode("400").
                setReasonPhrase("Bad Request").
                setHeader("Content-Type", "application/json").
                toString();
        assertEquals("HTTP/1.1 400 Bad Request\r\nContent-Type: application/json\r\n\r\n", message);
    }

    @Test
    void buildResponse500() {
        String message = new HTTPResponse("1.1").
                setStatusCode("500").
                setReasonPhrase("Internal Server Error").
                setHeader("Content-Type", "application/json").
                toString();
        assertEquals("HTTP/1.1 500 Internal Server Error\r\nContent-Type: application/json\r\n\r\n", message);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"id\": \"dog\"}",
            "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n",
            "HTTP/1.1 201 Created\r\nContent-Type: application/json\r\n\r\n{\"id\": \"dog\"}",
            "HTTP/1.1 201 Created\r\nContent-Type: application/json\r\n\r\n",
            "HTTP/1.1 204 No Content\r\nContent-Type: application/json\r\n\r\n",
            "HTTP/1.1 400 Bad Request\r\nContent-Type: application/json\r\n\r\n",
            "HTTP/1.1 500 Internal Server Error\r\nContent-Type: application/json\r\n\r\n"
    })
    void fromMessage(String message) {
        HTTPResponse response = HTTPResponse.fromMessage(message);
        assertEquals(message, response.toString());
    }
}