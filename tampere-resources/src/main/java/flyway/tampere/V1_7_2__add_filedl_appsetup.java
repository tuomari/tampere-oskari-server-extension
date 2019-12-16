package flyway.tampere;

import fi.nls.oskari.db.ViewHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V1_7_2__add_filedl_appsetup implements JdbcMigration {

    public void migrate(Connection connection) throws Exception {
        Logger logger = LogFactory.getLogger(this.getClass());
        long id = ViewHelper.insertView(connection, "filedownload.json");
        logger.info("Added filedownload appsetup with id", id);
    }
}
