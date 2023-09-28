package utility.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class HTTPRequestTest {

    @Test
    void buildGET1() {
        String message = new HTTPRequest("1.1").
                setMethod("GET").
                setURI("/data").
                setHeader("Host", "localhost:8080").
                setHeader("Accept", "application/json").
                toString();
        assertEquals("GET /data HTTP/1.1\r\nHost: localhost:8080\r\nAccept: application/json\r\n\r\n", message);
    }


    @Test
    void buildGET2() {
        String message = new HTTPRequest("1.1").
                setMethod("GET").
                setURI("/").
                setHeader("Host", "localhost:8080").
                toString();
        assertEquals("GET / HTTP/1.1\r\nHost: localhost:8080\r\n\r\n", message);
    }

    @Test
    void buildGET3() {
        String message = new HTTPRequest("1.1").
                setMethod("GET").
                setURI("localhost:8080/data").
                toString();
        assertEquals("GET localhost:8080/data HTTP/1.1\r\n\r\n", message);
    }

    @Test
    void buildPUT1() {
        String message = new HTTPRequest("1.1").
                setMethod("PUT").
                setURI("localhost:8080/data").
                setHeader("Accept", "application/json").
                setBody("{Animal: Dog}").
                toString();
        assertEquals("PUT localhost:8080/data HTTP/1.1\r\nAccept: application/json\r\n\r\n{Animal: Dog}", message);
    }


    @Test
    void buildPUT2() {
        String message = new HTTPRequest("1.1").
                setMethod("PUT").
                setURI("localhost:8080/data").
                setHeader("Accept", "application/json").
                setHeader("Content-Length", "12").
                setBody("{Animal: Dog}").
                toString();
        assertEquals("PUT localhost:8080/data HTTP/1.1\r\nAccept: application/json\r\nContent-Length: 12\r\n\r\n{Animal: Dog}", message);
    }

    @Test
    void buildPUT3() {
        String message = new HTTPRequest("1.1").
                setMethod("PUT").
                setURI("/data").
                setHeader("Host", "localhost:8080").
                setHeader("Accept", "application/json").
                setHeader("Content-Length", "12").
                setBody("{Animal: Dog}").
                toString();
        assertEquals("PUT /data HTTP/1.1\r\nHost: localhost:8080\r\nAccept: application/json\r\nContent-Length: 12\r\n\r\n{Animal: Dog}", message);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GET /data HTTP/1.1\r\nHost: localhost:8080\r\nAccept: application/json\r\n\r\n",
            "GET / HTTP/1.1\r\nHost: localhost:8080\r\n\r\n",
            "GET localhost:8080/data HTTP/1.1\r\n\r\n",
            "PUT localhost:8080/data HTTP/1.1\r\nAccept: application/json\r\n\r\n{Animal: Dog}",
            "PUT localhost:8080/data HTTP/1.1\r\nAccept: application/json\r\nContent-Length: 12\r\n\r\n{Animal: Dog}",
            "PUT /data HTTP/1.1\r\nHost: localhost:8080\r\nAccept: application/json\r\nContent-Length: 12\r\n\r\n{Animal: Dog}"
    })
    void fromMessage(String message) {
        HTTPRequest request = HTTPRequest.fromMessage(message);
        assertEquals(message, request.toString());
    }
}