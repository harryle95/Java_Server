package utility.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void getKeyExists() {
        Config config = new Config("src/config/server.properties");
        assertNotNull(config.get("archiveDir"));
    }

    @Test
    void getKeyNonExists(){
        Config Config = new Config("src/config/server.properties");
        assertNull(Config.get("archive"));
    }

    @Test
    void initNonExistConfig(){
        Config Config = new Config("src/config.properties");
        assertNull(Config.get("archiveDir"));
    }

    @Test
    void getDefaultValueConfigExists(){
        Config Config = new Config("src/config/server.properties");
        assertNotEquals(Config.get("archiveDir", "default"), "default");
    }

    @Test
    void getDefaultValueConfigNotExists(){
        Config Config = new Config("src/server.properties");
        assertEquals(Config.get("archiveDir", "default"), "default");
    }
}