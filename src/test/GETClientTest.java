import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GETClientTest {

    @Test
    void testNoArgument() {
        assertThrows(RuntimeException.class, () -> {
            GETClient client = new GETClient("".split(" "));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "localhost:8080 data",
            "http://localhost:8080 data",
            "http://localhost.com:8080 data",
            "http://localhost.com.au:8080 data",
    })
    void testIDProvided(String input) {
        GETClient client = new GETClient(input.split(" "));
        assertEquals("""
                GET /data HTTP/1.1\r
                Host: localhost:8080\r
                Accept: application/json\r
                \r
                """, client.formatMessage().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "localhost:8080",
            "http://localhost:8080",
            "http://localhost.com:8080",
            "http://localhost.com.au:8080",
    })
    void testNoID(String input) {
        GETClient client = new GETClient(input.split(" "));
        assertEquals("""
                GET / HTTP/1.1\r
                Host: localhost:8080\r
                Accept: application/json\r
                \r
                """, client.formatMessage().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "localhost:8080 data",
            "http://localhost:8080 data",
            "http://localhost.com:8080 data",
            "http://localhost.com.au:8080 data",
    })
    void testGetters(String input) {
        GETClient client = new GETClient(input.split(" "));
        assertEquals("localhost", client.getHostname());
        assertEquals(8080, client.getPort());
        assertEquals("data", client.getStationID());
    }
}