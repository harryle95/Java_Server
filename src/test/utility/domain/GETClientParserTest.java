package utility.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GETClientParserTest {
    GETClientParser parser;

    @BeforeEach
    void setUpParser() {
        parser = new GETClientParser();
    }

    @ParameterizedTest
    @CsvSource({
            "'localhost:8080', 'localhost', 8080",
            "'http://localhost:8080', 'localhost', 8080",
            "'http://localhost.domain:8080', 'localhost', 8080",
            "'http://localhost.domain1.domain2:8080', 'localhost', 8080",})
    void testNoID(String args, String hostname, int port) {
        String[] argv = args.split(" ");
        GETServerInformation info = parser.parse(argv);
        assertEquals(hostname, info.hostname);
        assertEquals(port, info.port);
    }

    @ParameterizedTest
    @CsvSource({
            "localhost:8080 1234",
            "http://localhost:8080 1234",
            "http://localhost.domain:8080 1234",
            "http://localhost.domain1.domain2:8080 1234",})
    void testWithID(String args) {
        String[] argv = args.split(" ");
        GETServerInformation info = parser.parse(argv);
        assertEquals("localhost", info.hostname);
        assertEquals(8080, info.port);
        assertEquals("1234", info.stationID);
    }

    @ParameterizedTest
    @CsvSource({
            "localhost",
            "port",
            "www.google.com",
            "localhost:8080:1093",
            "localhost:8080:1903:123",
            "http://localhost",
            "http:/localhost:8080",
            "localhost:123:8080 1234",
            "http//localhost:asd asda",
            "http://localhost.domain: asd",
            "http://localhost:domain1.domain2:8080 1234",
    })
    void testRuntimeException(String args) {
        String[] argv = args.split(" ");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            parser.parse(argv);
        });
    }

}