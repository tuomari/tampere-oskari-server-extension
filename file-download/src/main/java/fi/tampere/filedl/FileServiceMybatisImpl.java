package fi.tampere.filedl;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileServiceMybatisImpl extends FileService {

    private static final Class<FileMapper> MAPPER = FileMapper.class;
    private static final Logger LOG = LogFactory.getLogger(FileServiceMybatisImpl.class);

    private final SqlSessionFactory factory;

    public FileServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public FileServiceMybatisImpl(DataSource ds) {
        super();
        if (ds == null) {
            LOG.warn("DataSource was null, all future calls will throw NPEs!");
            factory = null;
        } else {
            factory = MyBatisHelper.initMyBatis(ds, MAPPER);
        }
    }

    public WFSAttachment insertFile(WFSAttachmentFile file) throws ServiceException {
        try (SqlSession session = factory.openSession(false)) {
            // save to db
            FileMapper mapper = session.getMapper(MAPPER);
            mapper.insertFile(file);
            // save to filesystem
            WFSAttachment attachment = super.insertFile(file);
            session.commit();
            // return result
            return attachment;
        }
    }

    public void updateMetadata(WFSAttachment file) {
        try (SqlSession session = factory.openSession(false)) {
            // save to db
            FileMapper mapper = session.getMapper(MAPPER);
            mapper.update(file);
        }
    }

    public List<Integer> getLayersWithFiles() {
        Set<Integer> layerIds = new HashSet<>();
        try (SqlSession session = factory.openSession()) {
            layerIds.addAll(session.getMapper(MAPPER).findLayersWithFiles());
            layerIds.addAll(session.getMapper(MAPPER).findLayersWithExternalFilepath());
        }
        return new ArrayList<>(layerIds);
    }

    public List<WFSAttachment> getFiles(int layerId) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).findByLayer(layerId);
        }
    }

    public List<WFSAttachment> getFiles(int layerId, String featureId) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).findByLayerAndFeature(layerId, featureId);
        }
    }

    public WFSAttachmentFile getFile(int layerId, int fileId) throws ServiceException {
        WFSAttachmentFile file = super.getFile(layerId, fileId);
        try (SqlSession session = factory.openSession()) {
            WFSAttachment metadata = session.getMapper(MAPPER).findFile(fileId);
            file.setFeatureId(metadata.getFeatureId());
            file.setLocale(metadata.getLocale());
            file.setFileExtension(metadata.getFileExtension());
            return file;
        }
    }

    public WFSAttachment removeFile(int layerId, int fileId) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            WFSAttachment metadata = session.getMapper(MAPPER).findFile(fileId);
            // remove from disk
            WFSAttachment file = super.removeFile(layerId, fileId);
            file.setFeatureId(metadata.getFeatureId());
            file.setLocale(metadata.getLocale());
            file.setFileExtension(metadata.getFileExtension());

            // remove from db
            session.getMapper(MAPPER).deleteFile(fileId);
            session.commit();
            return file;
        }
    }

}
