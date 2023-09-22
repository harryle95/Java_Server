package utility.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final Pattern pattern = Pattern.compile(
            "\"(\\w+)\": ?(\"[^\"]*\"|[^,\\n}]+),?$"
    );
    private Map<String, WeatherData> container;
    private WeatherData data;

    public Parser() {
        container = new LinkedHashMap<>();
        data = new WeatherData();
    }

    public void clear() {
        container.clear();
        data.clear();
    }

    public WeatherData get(String key){
        return container.get(key);
    }

    public int size(){
        return container.size();
    }

    public void parseFile(String fileName) throws FileNotFoundException {
        clear();
        File jsonFile = new File(fileName);
        Scanner reader = new Scanner(jsonFile);
        while (reader.hasNextLine()){
            parseLine(reader.nextLine());
        }
        // Put the last item if valid
        if (data.containsKey("id"))
            container.put(data.getID(), data);
    }

    public void parseString(String message) {
        clear();
        String[] splitMessage = message.split("\n");
        for (String line : splitMessage) {
            parseLine(line);
        }
        // Put the last item if valid
        if (data.containsKey("id"))
            container.put(data.getID(), data);
    }

    private void parseLine(String message) {
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
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("{\n");
        Iterator<Map.Entry<String, WeatherData>> iterator =
                container.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, WeatherData> entry = iterator.next();
            builder.append(entry.getValue().toString());
            if (iterator.hasNext())
                builder.append(",\n");
        }
        builder.append("\n}");
        return builder.toString();
    }

}
