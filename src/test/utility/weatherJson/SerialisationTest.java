package utility.weatherJson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class SerialisationTest {
    Map<String, Map<String, Map<String, String>>> stringMap;

    @BeforeEach
    void setUp(){
        stringMap = new HashMap<>();
        stringMap.put("Server1", new HashMap<>());
        stringMap.put("Server2", new HashMap<>());
        stringMap.get("Server1").put("File1", new HashMap<>());
        stringMap.get("Server1").put("File2", new HashMap<>());
        stringMap.get("Server2").put("File3", new HashMap<>());
        stringMap.get("Server2").put("File4", new HashMap<>());
        stringMap.get("Server1").get("File1").putAll(Map.of(
                "timestamp", "10",
                "value", "LongString"
        ));
    }

    @Test
    void testSerialising() throws IOException, ClassNotFoundException {
        FileOutputStream fout = new FileOutputStream("src/test/utility/weatherJson/resources/testBackUp");
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(stringMap);
        out.close();
        FileInputStream fis = new FileInputStream("src/test/utility/weatherJson/resources/testBackUp");
        ObjectInputStream in = new ObjectInputStream(fis);
        Map<String, Map<String, Map<String, String>>> backup = (Map<String, Map<String, Map<String, String>>>) in.readObject();
        Assertions.assertEquals(stringMap, backup);
    }
}
