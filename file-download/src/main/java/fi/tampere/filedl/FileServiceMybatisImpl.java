package fi.tampere.filedl;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.util.List;

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

    public WFSAttachment insertFile(int layerId, WFSAttachmentFile file) throws ServiceException {
        try (SqlSession session = factory.openSession(false)) {
            // save to db
            FileMapper mapper = session.getMapper(MAPPER);
            mapper.insertFile(layerId, file);
            // save to filesystem
            WFSAttachment attachment = super.insertFile(layerId, file);
            session.commit();
            // return result
            return attachment;
        }
    }

    public void updateMetadata(WFSAttachment file) throws ServiceException {
        // TODO: impl update everything but file contents
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


}
