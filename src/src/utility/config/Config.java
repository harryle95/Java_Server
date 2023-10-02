package utility.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {
    private final Properties dict = new Properties();
    private final Logger logger = Logger.getLogger("ConfigParser");

    public Config(String config) {
        try (FileInputStream inputStream = new FileInputStream(config)) {
            dict.load(inputStream);
        } catch (IOException e) {
            logger.info(config + " not found");
        }
    }

    public String get(String key) {
        return dict.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return dict.getProperty(key) == null ? defaultValue : dict.getProperty(key);
    }
}
