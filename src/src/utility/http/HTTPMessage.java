package utility.http;

public interface HTTPMessage {

    public HTTPMessage setHeader(String key, String value);

    public String toString();
}
