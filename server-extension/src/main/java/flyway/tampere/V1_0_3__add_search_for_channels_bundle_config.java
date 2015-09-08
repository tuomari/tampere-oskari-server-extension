package flyway.tampere;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by markokuo on 7.9.2015.
 */
public class V1_0_3__add_search_for_channels_bundle_config implements JdbcMigration {
    private static final String BUNDLE_NAME = "search-from-channels";

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
                connection.prepareStatement("INSERT INTO portti_bundle (name, config, state, startup) values ('"+BUNDLE_NAME+"','{}','{}',' { " +
                        "\"instanceProps\": {" +
                        "    }, " +
                        "    \"title\": \"SearchFromChannelsBundle\", " +
                        "    \"bundleinstancename\": \"search-from-channels\", " +
                        "    \"fi\": \"search-from-channels\", " +
                        "    \"sv\": \"search-from-channels\", " +
                        "    \"en\": \"search-from-channels\", " +
                        "    \"bundlename\": \"search-from-channels\", " +
                        "    \"metadata\": { " +
                        "        \"Import-Bundle\": { " +
                        "            \"search-from-channels\": { " +
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
