package fi.tampere.filedl;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@OskariActionRoute("UploadFile")
public class UploadFile extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(UploadFile.class);
    private static final int KB = 1024 * 1024;
    private static final int MB = 1024 * KB;

    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * KB;


    private final String fileStorage = PropertyUtil.get("file.storage.folder", "/files");
    private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        List<FileItem> fileItems = getFileItems(params.getRequest());
        List<FileItem> files = fileItems.stream()
                .filter(f -> !f.isFormField())
                .collect(Collectors.toList());

        Map<String, String> parameters = getFormParams(fileItems);
        String layerId = parameters.get("layer");
        try {
            Path path = ensurePath(layerId);
            List<String> writtenFiles = saveFiles(files, path);
            ResponseHelper.writeResponse(params, new JSONArray(writtenFiles));
        } finally {
            fileItems.forEach(FileItem::delete);
        }
    }

    private Path ensurePath(String layer) throws ActionException {
        Path path = Paths.get(fileStorage, layer);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (Exception e) {
                throw new ActionException("Unable to create folder for " + layer, e );
            }
        }
        return path;
    }
    private List<String> saveFiles(List<FileItem> files, Path path) {
        List<String> names = new ArrayList<>();
        for (FileItem file: files) {
            String filename = file.getName();
            Path mpath = Paths.get(path.toString(), filename);
            try {
                Files.copy(file.getInputStream(), mpath);
                names.add(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return names;
    }

    private List<FileItem> getFileItems(HttpServletRequest request) throws ActionException {
        try {
            request.setCharacterEncoding("UTF-8");
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            return upload.parseRequest(request);
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
