package flyway.tampere;

import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Created by markokuo on 7.9.2015.
 */
public class V1_0_4__add_search_from_channels_bundle_to_view implements JdbcMigration {
    private static final String BUNDLE_ID = "search-from-channels";

    public void migrate(Connection connection)
            throws Exception {

        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}
