package utility.json;

public class Parser {
    public WeatherData parseFile(String fileName) {
        return null;
    }

    public WeatherData parseString(String message) {
        String[] splitMsg = message.split("\n");
        WeatherData data = new WeatherData();
        for (String msg : splitMsg) {
            if (msg.equals("{") | msg.equals("}")) {
                continue;
            }
            String[] line = msg.split(": ");
            data.put(line[0], line[1]);
        }
        return data;
    }
}
