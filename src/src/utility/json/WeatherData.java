package utility.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WeatherData {
    public final List<String> numericFields;
    private final Map<String, Object> fields;


    public WeatherData() {
        numericFields = new ArrayList<>(List.of(new String[]{
                "lat", "long", "air_temp", "apparent_t",
                "dewpt", "press", "rel_hum", "wind_spd_kmh", "wind_spd_kt"}));
        fields = new LinkedHashMap<>();
    }

    public Object put(String key, String value) {
        if (numericFields.contains(key)) {
            try {
                int intVal = Integer.parseInt(value);
                return fields.put(key, intVal);
            } catch (Exception e) {
                try {
                    float floatVal = Float.parseFloat(value);
                    return fields.put(key, floatVal);
                } catch (Exception exc) {
                    return fields.put(key, value);
                }
            }
        }
        return fields.put(key, value);
    }

    public boolean containsKey(String key) {
        return fields.containsKey(key);
    }

    public Object get(String key) {
        return fields.get(key);
    }

    public int size() {
        return fields.size();
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("{\n");
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (entry.getValue() instanceof Float)
                builder.append(String.format("\"%s\": %f,\n", entry.getKey(),
                        entry.getValue()));
            else if (entry.getValue() instanceof Integer)
                builder.append(String.format("\"%s\": %d,\n", entry.getKey(),
                        entry.getValue()));
            else
                builder.append(String.format("\"%s\": \"%s\",\n", entry.getKey(),
                        entry.getValue()));
        }
        builder.append("}");
        return builder.toString();
    }


}
