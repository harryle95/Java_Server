package utility;

public class FileMetadata {
    private final String remoteIP;
    private final String fileName;

    private final String timestamp;

    public FileMetadata(String remoteIP, String fileName, String timestamp) {
        this.remoteIP = remoteIP;
        this.fileName = fileName;
        this.timestamp = timestamp;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
