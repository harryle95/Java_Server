package utility.weatherJson;

import java.util.*;

/**
 * Object representing data confined to a stationID
 */
public class WeatherData {
    public final List<String> numericFields;
    private final Map<String, Object> fields;


    public WeatherData() {
        numericFields = new ArrayList<>(Arrays.asList(new String[]{
                "lat", "lon", "air_temp", "apparent_t",
                "dewpt", "press", "rel_hum", "wind_spd_kmh", "wind_spd_kt"}));
        fields = new LinkedHashMap<>();
    }

    /**
     * Put key value pair in fields
     * <p>
     * If key is numeric, try to convert to Integer or Float before putting
     * Otherwise put as String
     *
     * @param key   key
     * @param value value
     */
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

    /**
     * Check if weather data contains a key
     *
     * @param key key to check
     * @return true if exists
     */
    public boolean containsKey(String key) {
        return fields.containsKey(key);
    }

    /**
     * Get stationID
     *
     * @return stationID in String
     */
    public String getID() {
        return (String) fields.get("id");
    }

    /**
     * Check whether value defined in local_date_time_full is valid
     *
     * @return true if the value can be converted to long
     */
    public boolean hasValidTS() {
        String TS = String.valueOf(fields.get("local_date_time_full"));
        try {
            Long.parseUnsignedLong(TS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get value of local_date_time_full
     *
     * @return YYYYMMDDHHMMSS long value
     */
    public long getTS() {
        return Long.parseUnsignedLong((String) fields.get("local_date_time_full"));
    }

    /**
     * Clear all fields
     */
    public void clear() {
        fields.clear();
    }

    /**
     * Generate key:value string output based on field definition order
     * <p>
     * String fields are enclosed with " "
     * Float fields are 1dp
     *
     * @return string output with each entry in json format.
     */
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
