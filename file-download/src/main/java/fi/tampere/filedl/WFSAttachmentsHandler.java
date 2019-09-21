package fi.tampere.filedl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.oskari.log.AuditLog;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@OskariActionRoute("WFSAttachments")
public class WFSAttachmentsHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(WFSAttachmentsHandler.class);

    private static final int KB = 1024 * 1024;
    private static final int MB = 1024 * KB;
    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * KB;
    private static final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String PARAM_LAYER = "layerId";

    private FileService service;

    public void init() {
        super.init();
        service = new FileServiceMybatisImpl();
    }

    public void handleGet(ActionParameters params) throws ActionException {
        int layerId = params.getHttpParam(PARAM_LAYER, -1);
        if (layerId == -1) {
            // return list of layer ids with attachments
            List<Integer> layerIds = service.getLayersWithFiles();
            ResponseHelper.writeResponse(params, new JSONArray(layerIds));
            return;
        }
        int fileId = params.getHttpParam("fileId", -1);
        // return file
        if (fileId != -1) {
            try (WFSAttachmentFile file = service.getFile(layerId, fileId)) {
                HttpServletResponse response = params.getResponse();
                response.setContentType(getContentType(file.getFileExtension()));
                // attachment header
                response.addHeader("Content-Disposition", "attachment; filename=\"" + getFilename(file, params.getLocale().getLanguage()) + "\"");
                IOHelper.copy(
                        file.getFile(),
                        response.getOutputStream());
                response.getOutputStream().flush();
                response.getOutputStream().close();
                return;
            } catch (IOException | ServiceException e) {
                throw new ActionException("Error reading file", e);
            }
        }
        // list files
        String featureId = params.getHttpParam("featureId");
        JSONArray files = null;
        try {
            if (featureId == null) {
                files = getFilesForLayer(layerId);
            } else if (featureId != null) {
                files = getFilesForFeature(layerId, featureId);
            }

        } catch (Exception e) {
            LOG.error(e, "Error reading files");
            ResponseHelper.writeResponse(params, new JSONArray());
        }

        ResponseHelper.writeResponse(params, files);
    }

    private JSONArray getFilesForLayer(int layerId) throws Exception {
        String files = MAPPER.writeValueAsString(service.getFiles(layerId));
        return new JSONArray(files);
    }

    private JSONArray getFilesForFeature(int layerId, String featureId) throws Exception {
        String files = MAPPER.writeValueAsString(service.getFiles(layerId, featureId));
        return new JSONArray(files);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        // TODO: check permissions

        List<FileItem> fileItems = parseRequest(params.getRequest());
        Map<String, String> parameters = getFormParams(fileItems);
        fileItems = getFiles(fileItems);
        int layerId = ConversionHelper.getInt(parameters.get(PARAM_LAYER), -1);
        if (layerId == -1) {
            throw new ActionParamsException("Layer id required");
        }

        try {
            List<WFSAttachment> writtenFiles = new ArrayList<>(fileItems.size());
            for (FileItem f : fileItems) {
                WFSAttachmentFile file = new WFSAttachmentFile(f.getInputStream());
                String[] name = parseFeatureID(f.getName());
                file.setFeatureId(name[0]);
                file.setLayerId(layerId);
                file.setFileExtension(name[1]);
                String locale = parameters.get("locale_" + f.getName());
                if (locale == null) {
                    locale = name[0];
                }
                file.setLocale(locale);
                writtenFiles.add(service.insertFile(file));
            }

            AuditLog.user(params.getClientIp(), params.getUser()).
                    withParam("layer", layerId).
                    withParam("files", writtenFiles.stream().map(
                            f -> f.getFeatureId() + " (" + f.getLocale() + ")")
                            .collect(Collectors.joining(","))).
                    added(getName());
            ResponseHelper.writeResponse(params, new JSONArray(MAPPER.writeValueAsString(writtenFiles)));
        } catch (JSONException | IOException | ServiceException e) {
            throw new ActionException("Unable to save files", e);
        } finally {
            fileItems.forEach(FileItem::delete);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        // TODO: check permissions
        WFSAttachment file = new WFSAttachment();
        file.setId(params.getRequiredParamInt("fileId"));
        file.setLocale(params.getRequiredParam("locale"));
        service.updateMetadata(file);
        AuditLog.user(params.getClientIp(), params.getUser()).
                withParam("id", file.getId()).
                updated(getName());
    }

    private String[] parseFeatureID(String filename) {
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

    private String getContentType(String extension) {
        // return "application/json;charset=UTF-8";
        return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType("file." + extension);
    }
    private String getFilename(WFSAttachment file, String lang) {
        return getLayerName(file.getLayerId(), lang) + "_" + file.getFeatureId() + "_"
                + file.getId() + "." + file.getFileExtension();
    }

    private String getLayerName(int id, String lang) {
        try {
            OskariLayer layer = OskariComponentManager.getComponentOfType(OskariLayerService.class).find(id);
            return layer.getName(lang);
        } catch (Exception e) {
            LOG.warn(e, "Error getting layer name");
        }
        return Integer.toString(id);
    }

    private List<FileItem> parseRequest(HttpServletRequest request) throws ActionException {
        try {
            request.setCharacterEncoding("UTF-8");
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            return upload.parseRequest(request);
        } catch (UnsupportedEncodingException | FileUploadException e) {
            throw new ActionException("Failed to read request", e);
        }
    }

    private List<FileItem> getFiles(List<FileItem> fileItems) {
        return fileItems.stream()
                .filter(f -> !f.isFormField())
                .collect(Collectors.toList());
    }

    private Map<String, String> getFormParams(List<FileItem> fileItems) {
        return fileItems.stream()
                .filter(f -> f.isFormField())
                .collect(Collectors.toMap(
                        f -> f.getFieldName(),
                        f -> new String(f.get(), StandardCharsets.UTF_8)));
    }

}
