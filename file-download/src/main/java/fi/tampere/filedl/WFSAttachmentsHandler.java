package fi.tampere.filedl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
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

import javax.servlet.http.HttpServletRequest;
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

    private FileService service;

    public void init() {
        super.init();
        service = new FileServiceMybatisImpl();
    }

    public void handleGet(ActionParameters params) throws ActionException {
        int layerId = params.getRequiredParamInt("layerId");
        int fileId = params.getHttpParam("fileId", -1);
        // return file
        if (fileId != -1) {
            try {
                WFSAttachmentFile file = service.getFile(layerId, fileId);
                // from file.getExtension()?
                params.getResponse().setContentType("application/json;charset=UTF-8");
                IOHelper.copy(
                        file.getFile(),
                        params.getResponse().getOutputStream());
                params.getResponse().getOutputStream().flush();
                params.getResponse().getOutputStream().close();
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
        params.requireLoggedInUser();
        // TODO: check permissions

        List<FileItem> fileItems = getFileItems(params.getRequest());
        Map<String, String> parameters = getFormParams(fileItems);
        int layerId = ConversionHelper.getInt(parameters.get("layer"), -1);
        if (layerId == -1) {
            throw new ActionParamsException("Layer id required");
        }

        try {
            List<WFSAttachment> writtenFiles = new ArrayList<>(fileItems.size());
            for (FileItem f : fileItems) {
                WFSAttachmentFile file = new WFSAttachmentFile(f.getInputStream());
                file.setFileExtension(f.getName());
                writtenFiles.add(service.insertFile(layerId, file));
            }

            ResponseHelper.writeResponse(params, new JSONArray(MAPPER.writeValueAsString(writtenFiles)));
        } catch (JSONException | IOException | ServiceException e) {
            throw new ActionException("Unable to save files", e);
        } finally {
            fileItems.forEach(FileItem::delete);
        }
    }


    private List<FileItem> getFileItems(HttpServletRequest request) throws ActionException {
        try {
            request.setCharacterEncoding("UTF-8");
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            return upload.parseRequest(request).stream()
                    .filter(f -> !f.isFormField())
                    .collect(Collectors.toList());
        } catch (UnsupportedEncodingException | FileUploadException e) {
            throw new ActionException("Failed to read request", e);
        }
    }

    private Map<String, String> getFormParams(List<FileItem> fileItems) {
        return fileItems.stream()
                .filter(f -> f.isFormField())
                .collect(Collectors.toMap(
                        f -> f.getFieldName(),
                        f -> new String(f.get(), StandardCharsets.UTF_8)));
    }

}
