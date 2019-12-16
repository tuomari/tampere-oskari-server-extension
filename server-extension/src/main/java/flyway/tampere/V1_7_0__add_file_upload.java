package flyway.tampere;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

public class V1_7_0__add_file_upload implements JdbcMigration {
    private static final String BUNDLE_ID = "file-upload";

    public void migrate(Connection connection) throws Exception {
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        BundleHelper.registerBundle(bundle, connection);

        // add bundle to default/user views
        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}
