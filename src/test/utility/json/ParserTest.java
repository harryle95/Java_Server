package utility.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "{\n\"id\": \"A0\",\n\"lat\": -34.9,\n\"wind_spd_kt\": 8\n}",
            "{\n\"id\": \"A0\"\n}",
            "{\n\"id\": \"A0\",\n\"message\": \"And he said to me: \"Don't go\"\"\n}",
            "{\n\"id\": \"A0\",\n\"value\": \"${{value}}\"\n}",
    })
    void parseString(String input) {
        WeatherData data = Parser.parseString(input);
        assertEquals(input, data.toString());
    }
}