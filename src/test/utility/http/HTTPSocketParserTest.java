package utility.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HTTPSocketParserTest {

    HTTPSocketParser parser;
    String GETRequestBlank;

    String GETRequestNonBlank;

    String POSTRequest;

    String PUTRequest;

    String PUTResponse201;

    String PUTResponse200;

    String GETResponse200;

    String GETResponse204;

    String GETResponse404;

    List<String> messages;


    @BeforeEach
    void fixture() {
        parser = new HTTPSocketParser();
        PUTRequest = "PUT /src/test/utility/weatherJson/resources/twoID.txt HTTP/1" +
                ".1\r\n" +
                "Host: 127.0.0.1:4567\r\n" +
                "Accept: application/json\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 122\r\n" +
                "Lamport-Clock: 5\r\n" +
                "\r\n" +
                "{\n" +
                "\"id\": \"A0\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\",\n" +
                "\"id\": \"A1\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\"\n" +
                "}";

        POSTRequest = "POST /src/test/utility/weatherJson/resources/twoID.txt HTTP/1" +
                ".1\r\n" +
                "Host: 127.0.0.1:4567\r\n" +
                "Accept: application/json\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 122\r\n" +
                "Lamport-Clock: 5\r\n" +
                "\r\n" +
                "{\n" +
                "\"id\": \"A0\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\",\n" +
                "\"id\": \"A1\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\"\n" +
                "}";

        GETRequestNonBlank = "GET /A0 HTTP/1.1\r\n" +
                "Host: 127.0.0.1:4567\r\n" +
                "Accept: application/json\r\n" +
                "Lamport-Clock: 1\r\n" +
                "\r\n";

        GETRequestBlank = "GET / HTTP/1.1\r\n" +
                "Host: 127.0.0.1:4567\r\n" +
                "Accept: application/json\r\n" +
                "Lamport-Clock: 1\r\n" +
                "\r\n";

        GETResponse200 = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 62\r\n" +
                "Lamport-Clock: 9\r\n" +
                "\r\n" +
                "{\n" +
                "\"id\": \"A0\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\"\n" +
                "}";

        GETResponse204 = "HTTP/1.1 204 No Content\r\n" +
                "Content-Type: application/json\r\n" +
                "Lamport-Clock: 3\r\n" +
                "\r\n";

        GETResponse404 = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: application/json\r\n" +
                "Lamport-Clock: 3\r\n" +
                "\r\n";

        PUTResponse201 = "HTTP/1.1 201 Created\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 122\r\n" +
                "Lamport-Clock: 7\r\n" +
                "\r\n" +
                "{\n" +
                "\"id\": \"A0\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\",\n" +
                "\"id\": \"A1\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\"\n" +
                "}";

        PUTResponse200 = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 122\r\n" +
                "Lamport-Clock: 13\r\n" +
                "\r\n" +
                "{\n" +
                "\"id\": \"A0\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\",\n" +
                "\"id\": \"A1\",\n" +
                "\"lat\": 10,\n" +
                "\"lon\": 20.2,\n" +
                "\"wind_spd_kt\": \"0x00f\"\n" +
                "}";
        messages = new ArrayList<>();
        messages.add(PUTRequest);
        messages.add(PUTResponse200);
        messages.add(PUTResponse201);
        messages.add(POSTRequest);
        messages.add(GETRequestBlank);
        messages.add(GETRequestNonBlank);
        messages.add(GETResponse200);
        messages.add(GETResponse204);
        messages.add(GETResponse404);
    }

    void testParsing(String message) {
        List<String> lines = new ArrayList<>(Arrays.asList(message.split("\n")));
        for (String line : lines)
            parser.parseLine(line);
        assertTrue(parser.isComplete());
        assertEquals(message, parser.toString());
    }


    @Test
    void testParsePUTRequest() {
        String message = PUTRequest;
        testParsing(message);
    }

    @Test
    void testParseGETRequestBlank() {
        String message = GETRequestBlank;
        testParsing(message);
    }

    @Test
    void testParseGETRequestNonBlank() {
        String message = GETRequestNonBlank;
        testParsing(message);
    }

    @Test
    void testParseGETResponse200() {
        String message = GETResponse200;
        testParsing(message);
    }

    @Test
    void testParseGETResponse204() {
        String message = GETResponse204;
        testParsing(message);
    }

    @Test
    void testParseGETResponse404() {
        String message = GETResponse404;
        testParsing(message);
    }

    @Test
    void testParsePUTResponse200() {
        String message = PUTResponse200;
        testParsing(message);
    }

    @Test
    void testParsePUTResponse201() {
        String message = PUTResponse201;
        testParsing(message);
    }

    @Test
    void testPostRequest() {
        String message = POSTRequest;
        testParsing(message);
    }

    @Test
    void testAllRequestResponse() {
        for (String message : messages) {
            testParsing(message);
            parser.reset();
        }
    }
}