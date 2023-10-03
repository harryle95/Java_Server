package utility.weatherJson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class WeatherDataTest {
    Parser parser;

    @BeforeEach
    void setUp() {
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
        parser.parseMessage(input);
        WeatherData data = parser.get("A0");
        assertEquals(input.substring(2, input.length() - 2), data.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\n\"id\": \"A0\",\n\"local_date_time_full\": \"123\"\n}",
            "{\n\"id\": \"A0\",\n\"local_date_time_full\": \"20230715160000\"\n}",
            "{\n\"id\": \"A0\",\n\"local_date_time_full\": \"20230715163000\"\n}",
    })
    void hasValidTSTrue(String input) {
        parser.parseMessage(input);
        WeatherData data = parser.get("A0");
        assertTrue(data.hasValidTS());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\n\"id\": \"A0\"\n}",
            "{\n\"id\": \"A0\",\n\"local_date_time_full\": \"invalid\"\n}",
            "{\n\"id\": \"A0\",\n\"locale_date_time_full\": \"12356\"\n}",
            "{\n\"id\": \"A0\",\n\"local_date_time_full\": \"-12356\"\n}",
            "{\n\"id\": \"A0\",\n\"locale_date_time_full\": \"1.2356f\"\n}",
            "{\n\"id\": \"A0\",\n\"locale_date_time_full\": \"1.2356\"\n}",
    })
    void hasValidTSFalse(String input) {
        parser.parseMessage(input);
        WeatherData data = parser.get("A0");
        assertFalse(data.hasValidTS());
    }

    @ParameterizedTest
    @CsvSource({
            "'{\n\"id\": \"A0\",\n\"local_date_time_full\": \"123\"\n}', 123",
            "'{\n" +
            "\"id\": \"A0\",\n" +
            "\"local_date_time_full\": \"20230715160000\"\n" +
            "}', 20230715160000",
            "'{\n" +
            "\"id\": \"A0\",\n" +
            "\"local_date_time_full\": \"20230715163000\"\n" +
            "}', 20230715163000",
    })
    void getTS(String input, float expected) {
        parser.parseMessage(input);
        WeatherData data = parser.get("A0");
        assertEquals(expected, data.getTS());
    }
}