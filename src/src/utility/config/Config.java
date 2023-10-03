package utility.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Config parser class. Used to store config data
 */
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

    /**
     * Look up key
     *
     * @param key
     * @return value if contained else null
     */
    public String get(String key) {
        return dict.getProperty(key);
    }

    /**
     * Look up key with default value
     *
     * @param key
     * @param defaultValue
     * @return value stored in config or default value
     */
    public String get(String key, String defaultValue) {
        return dict.getProperty(key) == null ? defaultValue : dict.getProperty(key);
    }
}
