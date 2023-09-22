package utility.json;

import java.util.*;

public class WeatherData {
    public final List<String> numericFields;
    private final Map<String, Object> fields;


    public WeatherData() {
        numericFields = new ArrayList<>(List.of(new String[]{
                "lat", "lon", "air_temp", "apparent_t",
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

    public String getID() {
        return (String) fields.get("id");
    }

    public boolean hasValidTS() {
        String TS;
        if ((TS = String.valueOf(fields.get("local_date_time_full"))) != null) {
            try {
                Float.parseFloat(TS);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public float getTS() {
        return (float) fields.get("local_date_time_full");
    }

    public void clear() {
        fields.clear();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, Object>> iterator = fields.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() instanceof Float)
                builder.append(String.format("\"%s\": %.1f", entry.getKey(),
                        entry.getValue()));
            else if (entry.getValue() instanceof Integer)
                builder.append(String.format("\"%s\": %d", entry.getKey(),
                        entry.getValue()));
            else
                builder.append(String.format("\"%s\": \"%s\"", entry.getKey(),
                        entry.getValue()));
            if (iterator.hasNext())
                builder.append(",\n");
        }
        return builder.toString();
    }


}
