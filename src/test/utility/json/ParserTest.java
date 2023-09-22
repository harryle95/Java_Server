package utility.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {
    Parser parser;
    private Path workDir;

    @BeforeEach
    void initPath() {
        workDir = Path.of("", "src/test/utility/json/resources");
    }

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
        assertEquals(input, parser.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\n\"id\": \"A0\",\n\"lat\": -34.9,\n\"id\": \"A1\",\n\"lat\": -34.9\n}",
            "{\n\"id\": \"A0\",\n\"id\": \"A1\",\n\"id\": \"A2\"\n}",
            "{\n\"id\": \"A0\",\n\"id\": \"A1\",\n\"id\": \"A2\",\n\"lat\": -34.9\n}",
    })
    void testParseMultiple(String input) {
        parser.parseMessage(input);
        assertEquals(input, parser.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\n\"id\": \"A0\",\n\"lat\": -34.9,\n\"lat\": -35.9\n}",
            "{\n\"lat\": -35.9,\n\"id\": \"A0\",\n\"lat\": -34.9\n}",
            "{\n\"lat\": -35.9,\n\"lon\": 10,\n\"id\": \"A0\",\n\"lat\": -34.9\n}",

    })
    void testParseInvalid(String input) {
        parser.parseMessage(input);
        assertEquals(1, parser.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "oneID.txt",
            "twoID.txt",
            "twoIDNotInOrder.txt",
//            "secondMissingID.txt",
            "firstMissingID.txt"
    })
    void testParseFile(String fileName) throws IOException {
        Path filePath = workDir.resolve(fileName);
        parser.parseFile(filePath);
        Path expFilePath = workDir.resolve("exp" + fileName);
        assertEquals(String.join("\n", Files.readAllLines(expFilePath)),
                parser.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "sameID_old_F_new_T.txt",
            "sameID_old_T_new_F.txt",
            "sameID_old_F_new_F.txt",
            "sameID_old_T_new_T_old_gt_new.txt",
            "sameID_old_T_new_T_old_lt_new.txt",
            "sameID_old_T_new_T_old_eq_new.txt"
    })
    void testParseFileSameID(String fileName) throws IOException {
        Path filePath = workDir.resolve(fileName);
        parser.parseFile(filePath);
        Path expFilePath = workDir.resolve("exp" + fileName);
        assertEquals(String.join("\n", Files.readAllLines(expFilePath)),
                parser.toString());
    }
}