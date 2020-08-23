package flyway.tampere;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.util.List;

/**
 * Created by markokuo on 7.9.2015.
 */
public class V1_8_0__add_mylocation_plugin_to_mapfull_bundle_config implements JdbcMigration {
    private ViewService service;
    private static final String PLUGIN_NAME = "Oskari.mapframework.bundle.mapmodule.plugin.MyLocationPlugin";

    public void migrate(Connection connection)
            throws Exception {
        service = new AppSetupServiceMybatisImpl();
        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            updateView(viewId);
        }
    }

    private void updateView(long viewId)
            throws Exception {

        View view = service.getViewWithConf(viewId);

        final Bundle mapfull = view.getBundleByName(ViewModifier.BUNDLE_MAPFULL);
        boolean addedPlugin = addPlugin(mapfull);
        if(addedPlugin) {
            service.updateBundleSettingsForView(view.getId(), mapfull);
        }
    }

    private boolean addPlugin(final Bundle mapfull) throws JSONException {
        final JSONObject config = mapfull.getConfigJSON();
        final JSONArray plugins = config.optJSONArray("plugins");
        if(plugins == null) {
            throw new RuntimeException("No plugins" + config.toString(2));
            //continue;
        }
        boolean found = false;
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.getJSONObject(i);
            if(PLUGIN_NAME.equals(plugin.optString("id"))) {
                found = true;
                break;
            }
        }
        // add plugin if not there yet
        if(!found) {
            plugins.put(JSONHelper.createJSONObject("id", PLUGIN_NAME));
        }
        return true;
    }

}
