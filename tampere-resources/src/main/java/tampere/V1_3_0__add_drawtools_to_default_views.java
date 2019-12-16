package flyway.tampere;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class V1_3_0__add_drawtools_to_default_views implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_3_0__add_drawtools_to_default_views.class);
    private static final String BUNDLE_ID = "drawtools";
    private int updatedViewCount = 0;

    public void migrate(Connection connection) throws Exception {
        try {
            updateViews(connection);
        }
        finally {
            LOG.info("Updated views:", updatedViewCount);
        }
    }

    private void updateViews(Connection conn) throws Exception {
        List<View> list = getViews(conn);
        LOG.info("Got", list.size(), "outdated views");
        for(View view : list) {
            addDrawToolsBundle(conn, view.getId());
            updatedViewCount++;
        }
    }

    private List<View> getViews(Connection conn) throws SQLException {
        List<View> list = new ArrayList<>();
        final String sql = "SELECT id FROM portti_view WHERE (type = 'USER' OR type = 'DEFAULT') AND id NOT IN (SELECT distinct view_id FROM portti_view_bundle_seq WHERE bundle_id IN (SELECT id FROM portti_bundle WHERE name='drawtools'));";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    View view = new View();
                    view.setId(rs.getLong("id"));
                    list.add(view);
                }
            }
        }
        return list;
    }

    public void addDrawToolsBundle(Connection conn, final long viewId) throws SQLException {
        Bundle bundle = BundleHelper.getRegisteredBundle(BUNDLE_ID, conn);
        if(bundle == null) {
            // not even registered so migration not needed
            return;
        }

        // remove drawtools bundle
        final String sql = "INSERT INTO portti_view_bundle_seq(view_id, bundle_id, seqno, config, \"state\", startup, bundleinstance) VALUES (?, ?, (SELECT MAX(seqno)+1 FROM portti_view_bundle_seq WHERE view_id = ?), null, null, ?, ?)";

        try (PreparedStatement statement =
                     conn.prepareStatement(sql)){
            statement.setLong(1, viewId);
            statement.setLong(2, bundle.getBundleId());
            statement.setLong(3, viewId);
            statement.setString(4, bundle.getStartup());
            statement.setString(5, BUNDLE_ID);
            statement.executeUpdate();
        }

    }
}
