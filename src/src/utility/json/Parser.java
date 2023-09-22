package utility.json;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static final Pattern pattern = Pattern.compile(
            "\"(\\w+)\": ?(\"[^\"]*\"|[^,\\n}]+)(,|\\n|})"
    );

    public static WeatherData parseString(String message) {
        WeatherData data = new WeatherData();
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String value = matcher.group(2);
            if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
                value = value.substring(1, value.length() - 1);
            data.put(matcher.group(1), value);
        }
        return data;
    }

    public WeatherData parseFile(String fileName) {
        return null;
    }
}
