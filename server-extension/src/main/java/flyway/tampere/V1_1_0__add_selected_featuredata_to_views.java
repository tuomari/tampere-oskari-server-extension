package flyway.tampere;

import java.sql.Connection;
import java.util.List;

import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import fi.nls.oskari.map.view.ViewService;

public class V1_1_0__add_selected_featuredata_to_views  implements JdbcMigration {

	private static final  String SELECTED_FEATUREDATA = "selected-featuredata";

	public void migrate(Connection connection) throws Exception {

        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            if (FlywayHelper.viewContainsBundle(connection, SELECTED_FEATUREDATA, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, SELECTED_FEATUREDATA);
        }
	}

}
