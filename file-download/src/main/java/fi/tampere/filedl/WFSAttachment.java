package fi.tampere.filedl;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WFSAttachment {

    private int id = -1;
    private int layerId = -1;
    private String featureId;
    private String locale;
    private String fileExtension;

    @JsonIgnore
    public WFSAttachment getMetadata() {
        WFSAttachment attachment = new WFSAttachment();
        attachment.setId(getId());
        attachment.setFeatureId(getFeatureId());
        attachment.setLocale(getLocale());
        attachment.setFileExtension(getFileExtension());
        return attachment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLayerId() {
        return layerId;
    }

    public void setLayerId(int layerId) {
        this.layerId = layerId;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
