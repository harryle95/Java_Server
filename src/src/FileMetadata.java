public class FileMetadata {
    private final String remoteIP;
    private final String fileName;

    public FileMetadata(String remoteIP, String fileName) {
        this.remoteIP = remoteIP;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRemoteIP() {
        return remoteIP;
    }
}
