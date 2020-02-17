package fi.tampere.filedl;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileService {

    protected static final String KEY_ATTACHMENT_PATH = "attachmentPath";

    private static final Logger LOG = LogFactory.getLogger(FileService.class);
    private final String fileStorage = PropertyUtil.get("file.storage.folder", "/files");

    /**
     * Filename should be featureID + extension
     * @param filename
     * @return array of length 2 with base as first and extension as second
     */
    public static String[] getBaseAndExtension(String filename) {
        String[] name = filename.split("\\.");
        if (name.length > 2) {
            String ext = name[name.length - 1];
            String id = filename.substring(0, filename.length() - ext.length() - 1);
            return new String[] { id, ext };
        }
        if (name.length < 2) {
            return new String[] { name[0], "" };
        }
        return name;
    }

    public WFSAttachmentFile getFile(int layerId, int fileId) throws ServiceException {

        Path path = Paths.get(fileStorage, Integer.toString(layerId), Integer.toString(fileId));
        if (!Files.exists(path)) {
            throw new ServiceException("File not found");
        }
        try {
            // TODO: remember to close the inputstream after
            WFSAttachmentFile file = new WFSAttachmentFile(Files.newInputStream(path));
            file.setLayerId(layerId);
            file.setId(fileId);
            return file;
        } catch (IOException e) {
            throw new ServiceException("Error reading file", e);
        }
    }

    public WFSAttachmentFile getExternalFile(int layerId, String featureId, String name) throws ServiceException {
        Optional<Path> basePath = getPathForExternalFiles(layerId, featureId);
        if (!basePath.isPresent()) {
            throw new ServiceException("Error reading file");
        }
        // strip out any .. references
        String fname = Paths.get(name).getFileName().toString();
        Path filePath = Paths.get(basePath.get().toString(), fname);
        try {
            // TODO: remember to close the inputstream after
            WFSAttachmentFile file = new WFSAttachmentFile(Files.newInputStream(filePath));
            String[] filename = FileService.getBaseAndExtension(fname);
            file.setLocale(filename[0]);
            file.setFileExtension(filename[1]);
            file.setLayerId(layerId);
            file.setFeatureId(featureId);
            file.setExternal(true);
            return file;
        } catch (IOException e) {
            throw new ServiceException("Error reading file", e);
        }
    }

    public List<Integer> getLayersWithFiles() {
        throw new ServiceRuntimeException("Not implemented");
    }

    public List<WFSAttachment> getFiles(int layerId) {

        Path path = Paths.get(fileStorage, Integer.toString(layerId));
        if (!Files.exists(path)) {
            return Collections.EMPTY_LIST;
        }
        try {
            return Files.list(path)
                    .map(p -> p.toFile())
                    .filter(f -> f.canRead() && !f.isDirectory())
                    .map(f -> new WFSAttachment())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ServiceRuntimeException("Unable to read files", e);
        }
    }

    private Optional<Path> getPathForExternalFiles(int layerId, String featureId) {
        OskariLayer layer = OskariComponentManager.getComponentOfType(OskariLayerService.class).find(layerId);
        if(layer == null) {
            return Optional.empty();
        }
        final String layerPath = layer.getAttributes().optString(KEY_ATTACHMENT_PATH);
        if(layerPath == null) {
            return Optional.empty();
        }
        // layer_basepath/feature_id/format.zip
        Path path = Paths.get(layerPath, featureId);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        return Optional.of(path);
    }

    public List<WFSAttachment> getExternalFiles(int layerId, String featureId) {
        Optional<Path> path = getPathForExternalFiles(layerId, featureId);
        if (!path.isPresent()) {
            return Collections.EMPTY_LIST;
        }
        try {
            return Files.list(path.get())
                    .map(p -> p.toFile())
                    .filter(f -> f.canRead() && !f.isDirectory())
                    .map(f -> {
                        WFSAttachment file = new WFSAttachment();
                        String[] name = FileService.getBaseAndExtension(f.getName());
                        file.setLocale(name[0]);
                        file.setFileExtension(name[1]);
                        file.setFeatureId(featureId);
                        file.setLayerId(layerId);
                        file.setExternal(true);
                        return file;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ServiceRuntimeException("Unable to read files", e);
        }
    }

    public List<WFSAttachment> getFiles(int layerId, String featureId) {
        return getFiles(layerId).stream()
                .filter(f -> f.getFeatureId().equalsIgnoreCase(featureId))
                .collect(Collectors.toList());
    }


    public WFSAttachment insertFile(WFSAttachmentFile file) throws ServiceException {

        Path path = ensurePath(Integer.toString(file.getLayerId()));
        try {
            // save file
            String filename = Integer.toString(file.getId());
            LOG.info("File path for", filename);
            Path mpath = Paths.get(path.toString(), filename);
            LOG.info("Copy contents");
            Files.copy(file.getFile(), mpath);
            LOG.info("Copied contents");
            return file.getMetadata();
        } catch (IOException e) {
            throw new ServiceException("Unable to save files", e);
        }
    }

    public void updateMetadata(WFSAttachment file) {
        throw new ServiceRuntimeException("Not implemented");
    }

    private Path ensurePath(String layer) throws ServiceException {
        LOG.info("Checking path for", fileStorage, layer);
        Path path = Paths.get(fileStorage, layer);
        if (!Files.exists(path)) {
            LOG.info("Creating path");
            try {
                Files.createDirectory(path);
                LOG.info("Created path");
            } catch (Exception e) {
                throw new ServiceException("Unable to create folder for " + layer, e);
            }
        } else {
            LOG.info("Path existed");
        }
        return path;
    }

    public WFSAttachment removeFile(int layerId, int fileId) throws ServiceException {
        // WFSAttachment file = findFile(fileId);
        Path path = Paths.get(fileStorage, Integer.toString(layerId), Integer.toString(fileId));
        if (!Files.exists(path)) {
            throw new ServiceException("File not found");
        }
        path.toFile().delete();
        WFSAttachment file = new WFSAttachment();
        file.setLayerId(layerId);
        file.setId(fileId);
        return file;
    }

}
