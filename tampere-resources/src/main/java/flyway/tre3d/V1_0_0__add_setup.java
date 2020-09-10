package flyway.tre3d;

import fi.nls.oskari.db.ViewHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V1_0_0__add_setup implements JdbcMigration {
    public void migrate(Connection connection) throws Exception {
        ViewHelper.insertView(connection, "geoportal3d.json");
    }
}
