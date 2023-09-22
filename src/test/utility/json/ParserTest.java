package utility.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    Parser parser;
    @BeforeEach
    void setUp(){
        parser = new Parser();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\n\"id\": \"A0\",\n\"lat\": -34.9,\n\"wind_spd_kt\": 8\n}",
            "{\n\"id\": \"A0\",\n\"lat\": \"0x66f\",\n\"wind_spd_kt\": 8\n}",
            "{\n\"id\": \"A0\"\n}",
            "{\n\"id\": \"A0\",\n\"message\": \"And he said to me: \"Don't go\"\"\n}",
            "{\n\"id\": \"A0\",\n\"value\": \"${{value}}\"\n}",
    })
    void testParseSingleItem(String input) {
        parser.parseString(input);
        assertEquals(input, parser.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\n\"id\": \"A0\",\n\"lat\": -34.9,\n\"id\": \"A1\",\n\"lat\": -34.9\n}",
            "{\n\"id\": \"A0\",\n\"id\": \"A1\",\n\"id\": \"A2\"\n}",
            "{\n\"id\": \"A0\",\n\"id\": \"A1\",\n\"id\": \"A2\",\n\"lat\": -34.9\n}",
    })
    void testParseMultiple(String input) {
        parser.parseString(input);
        assertEquals(input, parser.toString());
    }
    @ParameterizedTest
    @ValueSource(strings = {
            "{\n\"id\": \"A0\",\n\"lat\": -34.9,\n\"lat\": -35.9\n}",
            "{\n\"lat\": -35.9,\n\"id\": \"A0\",\n\"lat\": -34.9\n}",
            "{\n\"lat\": -35.9,\n\"lon\": 10,\n\"id\": \"A0\",\n\"lat\": -34.9\n}",

    })
    void testParseInvalid(String input) {
        parser.parseString(input);
        assertEquals(1, parser.size());
    }
}