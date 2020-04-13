package flyway.tampere;

import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

public class V1_7_3__add_backendstatus implements JdbcMigration {
    private static final String BUNDLE_ID = "backendstatus";

    public void migrate(Connection connection) throws Exception {
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
