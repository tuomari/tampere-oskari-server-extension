package flyway.tampere;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by markokuo on 7.9.2015.
 */
public class V1_0_1__add_admin_wfs_search_channel_bundle_config implements JdbcMigration {
    private static final String BUNDLE_NAME = "admin-wfs-search-channel";

    public void migrate(Connection connection)
            throws Exception {
        // check existing value before adding
        final boolean bundleExists = bundleExists(connection);
        if (!bundleExists) {
            addBundle(connection);
        }
    }

    private boolean bundleExists(Connection connection)
            throws Exception {
        final PreparedStatement statement =
                connection.prepareStatement("SELECT * FROM portti_bundle WHERE name='" + BUNDLE_NAME + "'");
        try (ResultSet rs = statement.executeQuery()) {
            return rs.next();
        } finally {
            statement.close();
        }
    }

    private void addBundle(Connection connection)
            throws Exception {
        final PreparedStatement statement =
                connection.prepareStatement("INSERT INTO portti_bundle (name, config, state, startup) values('"+BUNDLE_NAME+ "','{}','{}',' {" +
                        "    \"instanceProps\": { " +
                        "    }, " +
                        "    \"title\": \"AdminWfsSearchChannel\", " +
                        "    \"bundleinstancename\": \"admin-wfs-search-channel\", " +
                        "    \"fi\": \"admin-wfs-search-channel\", " +
                        "    \"sv\": \"admin-wfs-search-channel\", " +
                        "    \"en\": \"admin-wfs-search-channel\", " +
                        "    \"bundlename\": \"admin-wfs-search-channel\", " +
                        "    \"metadata\": { " +
                        "        \"Import-Bundle\": { " +
                        "            \"admin-wfs-search-channel\": { " +
                        "                \"bundlePath\": \"/Oskari/packages/tampere/bundle/\" " +
                        "            } " +
                        "        }, " +
                        "        \"Require-Bundle-Instance\": [] " +
                        "    } " +
                        "}');");
        try {
            statement.execute();
        } finally {
            statement.close();
        }
    }
}
