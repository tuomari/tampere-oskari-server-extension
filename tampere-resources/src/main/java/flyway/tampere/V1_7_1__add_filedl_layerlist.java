package flyway.tampere;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V1_7_1__add_filedl_layerlist implements JdbcMigration {
    private static final String BUNDLE_ID = "file-layerlist";

    public void migrate(Connection connection) throws Exception {
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        BundleHelper.registerBundle(bundle, connection);
    }
}
