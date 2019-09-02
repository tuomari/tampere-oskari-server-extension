package fi.tampere.filedl;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
}
