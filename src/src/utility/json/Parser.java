package utility.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static final Pattern pattern = Pattern.compile(
            "\"(\\w+)\": ?(\"[^\"]*\"|[^,\\n}]+)(,|\\n|})"
    );

    public static Map<String, WeatherData> parseString(String message) {
        Map<String, WeatherData> container = new LinkedHashMap<>();
        WeatherData data = new WeatherData();
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);

            // Process Value
            if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
                value = value.substring(1, value.length() - 1);

            // Check if continue old entry or is new entry
            if (data.containsKey(key)) {
                if (data.containsKey("id"))
                    container.put(data.getID(), data); // Items only valid if has ID
                data = new WeatherData();
            }
            // Put Value
            data.put(key, value);
        }
        // Put the last item if valid
        if (data.containsKey("id"))
            container.put(data.getID(), data);
        return container;
    }

    public WeatherData parseFile(String fileName) {
        return null;
    }
}
