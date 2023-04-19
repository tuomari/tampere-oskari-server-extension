package flyway.tre;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.BundleHelper;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.JSONHelper;

public class V1_0_7__replace_search_with_search_plugin extends BaseJavaMigration {

    private static final String SEARCH_BUNDLE_NAME = "search";
    private static final String SEARCH_FROM_CHANNELS_BUNDLE_NAME = "search-from-channels";
    private static final String SEARCH_PLUGIN_NAME = "Oskari.mapframework.bundle.mapmodule.plugin.SearchPlugin";

    public void migrate(Context context) throws Exception {
        Connection c = context.getConnection();

        List<Long> appsetupIds = AppSetupHelper.getSetupsForUserAndDefaultType(c);
        for (long id : appsetupIds) {
            // Add search search plugin to mapfull if it doesn't exist
            Bundle mapfull = AppSetupHelper.getAppBundle(c, id, "mapfull");
            if (updateMapfullBundle(mapfull)) {
                AppSetupHelper.updateAppBundle(c, id, mapfull);
            }

            // Remove both search and search-from-channels from appsetup bundles
            AppSetupHelper.removeBundleFromApp(c, id, SEARCH_BUNDLE_NAME);
            AppSetupHelper.removeBundleFromApp(c, id, SEARCH_FROM_CHANNELS_BUNDLE_NAME);
        }

        BundleHelper.unregisterBundle(c, SEARCH_BUNDLE_NAME);
        BundleHelper.unregisterBundle(c, SEARCH_FROM_CHANNELS_BUNDLE_NAME);
    }

    private boolean updateMapfullBundle(Bundle mapfull) {
        if (mapfull == null) {
            return false;
        }

        JSONObject config = mapfull.getConfigJSON();
        if (config == null) {
            return false;
        }

        JSONArray plugins = JSONHelper.getJSONArray(config, "plugins");
        if (plugins == null || searchPluginExists(plugins)) {
            return false;
        }

        JSONObject searchPluginConfig = new JSONObject();
        JSONHelper.putValue(searchPluginConfig,
                "allowOptions", true);
        JSONHelper.putValue(searchPluginConfig,
                "columns", new JSONArray(Arrays.asList("selected", "name", "region", "type")));

        JSONObject searchPlugin = new JSONObject();
        JSONHelper.putValue(searchPlugin, "id", SEARCH_PLUGIN_NAME);
        JSONHelper.putValue(searchPlugin, "config", searchPluginConfig);

        plugins.put(searchPlugin);

        return true;
    }

    private boolean searchPluginExists(JSONArray plugins) {
        for (int i = 0; i < plugins.length(); i++) {
            JSONObject plugin = JSONHelper.getJSONObject(plugins, i);
            if (SEARCH_PLUGIN_NAME.equals(plugin.opt("id"))) {
                return true;
            }
        }
        return false;
    }

}
