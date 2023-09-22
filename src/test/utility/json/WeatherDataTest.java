package utility.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import static org.junit.jupiter.api.Assertions.*;

class WeatherDataTest {
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
        WeatherData data = parser.get("A0");
        assertEquals(input.substring(2, input.length()-2), data.toString());
    }
}