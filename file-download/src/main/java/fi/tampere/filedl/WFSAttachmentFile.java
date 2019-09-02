package fi.tampere.filedl;

import java.io.InputStream;

public class WFSAttachmentFile extends WFSAttachment {

    private InputStream file;

    public WFSAttachmentFile(InputStream file) {
        this.file = file;
    }

    public InputStream getFile() {
        return file;
    }
    public void setFile(InputStream in) {
        file = in;
    }
}
