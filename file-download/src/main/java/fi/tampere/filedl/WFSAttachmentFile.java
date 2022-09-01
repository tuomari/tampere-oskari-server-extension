package fi.tampere.filedl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class WFSAttachmentFile extends WFSAttachment implements Closeable {

    private InputStream file;

    public WFSAttachmentFile(InputStream file) {
        this.file = file;
    }

    public InputStream getFile() {
        return file;
    }

    @Override
    public void close() throws IOException {
        if (file != null) {
            file.close();
        }
    }
}
