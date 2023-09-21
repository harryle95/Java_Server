package utility.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ContentServerParserTest {
    ContentServerParser parser;
    @BeforeEach
    void setUpParser() {
        parser = new ContentServerParser();
    }

    @ParameterizedTest
    @CsvSource({
            "localhost:8080 file.json",
            "http://localhost:8080 file.json",
            "http://localhost.domain:8080 file.json",
            "http://localhost.domain1.domain2:8080 file.json",})
    void testWithID(String args) {
        String[] argv = args.split(" ");
        ContentServerInformation info = parser.parse(argv);
        assertEquals("localhost", info.hostname);
        assertEquals(8080, info.port);
        assertEquals("file.json", info.fileName);
    }
}