package utility.domain;

public class ContentServerInformation extends ServerInformation {
    public String fileName;

    public ContentServerInformation(String hostname, int port) {
        super(hostname, port);
    }

    public static ContentServerInformation fromServerInfo(ServerInformation item) {
        return new ContentServerInformation(item.hostname, item.port);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
