package flyway.tampere;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by markokuo on 7.9.2015.
 */
public class V1_0_4__add_search_from_channels_bundle_to_view implements JdbcMigration {
    private ViewService service = new ViewServiceIbatisImpl();

    public void migrate(Connection connection)
            throws Exception {
        // check existing value before adding
        final boolean bundleExistsInView = bundleExistsInView(1);
        if (!bundleExistsInView) {
            addBundleToView(connection);
        }
    }

    private boolean bundleExistsInView(long viewId)
            throws Exception {

        View view = service.getViewWithConf(viewId);
        final Bundle searchFromChannels = view.getBundleByName("search-from-channels");
        return searchFromChannels != null;
    }

    private void addBundleToView(Connection connection)
            throws Exception {
        final PreparedStatement statement =
                connection.prepareStatement("INSERT " +
                        "INTO portti_view_bundle_seq" +
                        "( " +
                        "    view_id, " +
                        "    bundle_id, " +
                        "    seqno, " +
                        "    config, " +
                        "    state, " +
                        "    bundleinstance " +
                        ") " +
                        "VALUES ( " +
                        "    1, " +
                        "    (SELECT id FROM portti_bundle WHERE name='search-from-channels'), " +
                        "    (SELECT max(seqno)+1 FROM portti_view_bundle_seq WHERE view_id=1), " +
                        "    (SELECT config FROM portti_bundle WHERE name='search-from-channels'), " +
                        "    (SELECT state FROM portti_bundle WHERE name='search-from-channels'), " +
                        "    'search-from-channels' " +
                        ")");
        try {
            statement.execute();
        } finally {
            statement.close();
        }
    }
}
