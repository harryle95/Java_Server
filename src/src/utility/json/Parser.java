package utility.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public Map<String, WeatherData> getContainer() {
        return container;
    }

    private final Map<String, WeatherData> container;
    private WeatherData data;

    public Parser() {
        container = new LinkedHashMap<>();
        data = new WeatherData();
    }

    /**
     * Reset current Parser's state. Used when parsing new message or file.
     */
    public void clear() {
        container.clear();
        data.clear();
    }

    /**
     * Get weather data record using id information.
     *
     * @param key station id used to identify weather recordings
     * @return WeatherData data record
     */
    public WeatherData get(String key) {
        return container.get(key);
    }

    /**
     * Get number of weather records extracted in the current state
     *
     * @return int number of weather recordings
     */
    public int size() {
        return container.size();
    }

    /**
     * Extract JSON from file
     *
     * @param filePath path to json file (txt format)
     * @throws IOException if file does not exist
     */
    public void parseFile(Path filePath) throws IOException {
        Pattern pattern = Pattern.compile(
                "(\\w+): ?([^\n]+) ?$"
        );
        List<String> splitMessage = Files.readAllLines(filePath);
        parseString(splitMessage, pattern);
    }

    /**
     * Extract JSON from HTTP message body
     *
     * @param message body of Content-Type application/json
     */
    public void parseMessage(String message) {
        Pattern pattern = Pattern.compile(
                "\"(\\w+)\": ?(\"[^\"]*\"|[^,\\n}]+),?$"
        );
        List<String> splitMessage = List.of(message.split("\n"));
        parseString(splitMessage, pattern);
    }

    /**
     * Process JSON file, containing a series of key value pairs.
     * Key value pairs are extracted using regex pattern.
     *
     * <p> All key values pertaining to a weather recording are wrapped using
     * WeatherData data, which is identified with id and recording timestamp
     * (local_date_time_full)
     * </p>
     *
     * @param splitMessage list of lines
     * @param pattern      pattern to process line
     */
    private void parseString(List<String> splitMessage, Pattern pattern) {
        clear();
        for (String line : splitMessage) {
            parseLine(line, pattern);
        }
        // Put the last item if valid
        putIfPermitted();
    }

    /**
     * Extract key value pair from current line using provided pattern.
     *
     * <p>
     * Extracted key value pair is added as another entry to current WeatherData data
     * . If
     * the same key is present in data, a new data is created, and current data is put
     * in the container if permitted.
     * </p>
     *
     * @param message current line to process
     * @param pattern pattern to extract key, value pair
     */
    private void parseLine(String message, Pattern pattern) {
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);

            // Process Value
            if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
                value = value.substring(1, value.length() - 1);

            // Check if continue old entry or is new entry
            if (data.containsKey(key)) {
                putIfPermitted();
                data = new WeatherData();
            }
            // Put Value
            data.put(key, value);
        }
    }

    /**
     * Circuit to test whether the current data can be put in container, and put if
     * permitted.
     * <p>
     * Current data can be put iff either no other data under the same ID is in the
     * container,
     * or if other data is present, it's timestamp (TS) denoted under
     * local_date_time_full
     * is of higher value (more recent)
     */
    private void putIfPermitted() {
        // Only consider putting new data if ID field is present
        if (data.containsKey("id")) {
            String id = data.getID();
            // Compare TS field between old and new if an old entry is present
            if (container.containsKey(id)) {
                WeatherData oldData = container.get(id);
                // If old data has no TS and new data has, use new data
                if (!oldData.hasValidTS() & data.hasValidTS()) {
                    container.put(id, data);
                    return;
                }
                // If both old and new has DT, choose the one with higher timestamp
                if (oldData.hasValidTS() && data.hasValidTS()) {
                    long oldTS = oldData.getTS();
                    long newTS = data.getTS();
                    if (newTS > oldTS) {
                        container.put(id, data);
                    }
                }
            } else { // Old entry not present, just put new one
                container.put(id, data);
            }
        }

    }


    /**
     * Generate JSON format message based on parser's current state
     *
     * @return String output where each entry is on a new line, including '{' and '}'
     */
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
