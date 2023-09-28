package utility.domain;

public class ContentServerInformation extends ServerInformation {
    public String fileName;

    public ContentServerInformation(ServerInformation info) {
        super(info);
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
