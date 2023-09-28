package utility;


import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

class FileMetadataTest {
    @Test
    void testGetters() {
        String host = "localhost";
        String file = "data.json";
        String ts = "15";
        FileMetadata metadata = new FileMetadata(host, file, ts);
        assertEquals(host, metadata.remoteIP());
        assertEquals(file, metadata.fileName());
        assertEquals(ts, metadata.timestamp());
    }
}