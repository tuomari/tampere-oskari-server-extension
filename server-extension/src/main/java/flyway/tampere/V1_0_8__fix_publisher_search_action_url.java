package flyway.tampere;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;

public class V1_0_8__fix_publisher_search_action_url implements JdbcMigration{
	 private static final Logger LOG = LogFactory.getLogger(V1_0_8__fix_publisher_search_action_url.class);
	    private ViewService service = new ViewServiceIbatisImpl();
	    private static final String PLUGIN_NAME = "Oskari.mapframework.bundle.mapmodule.plugin.SearchPlugin";

	    public void migrate(Connection connection)
	            throws Exception {
	        updateView(1);
	    }

	    private void updateView(long viewId)
	            throws Exception {

	        View view = service.getViewWithConf(viewId);

	        final Bundle publisher = view.getBundleByName("publisher");
	        boolean addedPlugin = modifyToolsConfig(publisher);
	        if(addedPlugin) {
	            service.updateBundleSettingsForView(view.getId(), publisher);
	        }
	    }

	    private boolean modifyToolsConfig(final Bundle publisher) throws JSONException {
	        final JSONObject config = publisher.getConfigJSON();
	        final JSONArray tools = config.optJSONArray("tools");
	        final JSONArray oldConfig = config.optJSONArray("config");
	        if(oldConfig != null){
	        	config.remove("config");
	        }
	        if(tools == null || tools.length() == 0) {
	            JSONArray addTools = new JSONArray("[{ " +
	                    "            \"id\": \"Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin\", " +
	                    "            \"selected\": false, " +
	                    "            \"lefthanded\": \"bottom left\", " +
	                    "            \"righthanded\": \"bottom right\", " +
	                    "            \"config\": { " +
	                    "                \"location\": { " +
	                    "                    \"top\": \"\", " +
	                    "                    \"right\": \"\", " +
	                    "                    \"bottom\": \"\", " +
	                    "                    \"left\": \"\", " +
	                    "                    \"classes\": \"bottom left\" " +
	                    "                } " +
	                    "            } " +
	                    "        }, { " +
	                    "            \"id\": \"Oskari.mapframework.bundle.mapmodule.plugin.IndexMapPlugin\", " +
	                    "            \"selected\": false, " +
	                    "            \"lefthanded\": \"bottom right\", " +
	                    "            \"righthanded\": \"bottom left\", " +
	                    "            \"config\": { " +
	                    "                \"location\": { " +
	                    "                    \"top\": \"\", " +
	                    "                    \"right\": \"\", " +
	                    "                    \"bottom\": \"\", " +
	                    "                    \"left\": \"\", " +
	                    "                    \"classes\": \"bottom right\" " +
	                    "                } " +
	                    "            } " +
	                    "        }, { " +
	                    "            \"id\": \"Oskari.mapframework.bundle.mapmodule.plugin.PanButtons\", " +
	                    "            \"selected\": false, " +
	                    "            \"lefthanded\": \"top left\", " +
	                    "            \"righthanded\": \"top right\", " +
	                    "            \"config\": { " +
	                    "                \"location\": { " +
	                    "                    \"top\": \"\", " +
	                    "                    \"right\": \"\", " +
	                    "                    \"bottom\": \"\", " +
	                    "                    \"left\": \"\", " +
	                    "                    \"classes\": \"top left\" " +
	                    "                } " +
	                    "            } " +
	                    "        }, { " +
	                    "            \"id\": \"Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar\", " +
	                    "            \"selected\": true, " +
	                    "            \"lefthanded\": \"top left\", " +
	                    "            \"righthanded\": \"top right\", " +
	                    "            \"config\": { " +
	                    "                \"location\": { " +
	                    "                    \"top\": \"\", " +
	                    "                    \"right\": \"\", " +
	                    "                    \"bottom\": \"\", " +
	                    "                    \"left\": \"\", " +
	                    "                    \"classes\": \"top left\" " +
	                    "                } " +
	                    "            } " +
	                    "        }, { " +
	                    "            \"id\": \"Oskari.mapframework.bundle.mapmodule.plugin.MyLocationPlugin\", " +
	                    "            \"selected\": false, " +
	                    "            \"lefthanded\": \"top left\", " +
	                    "            \"righthanded\": \"top right\", " +
	                    "            \"config\": { " +
	                    "                \"location\": { " +
	                    "                    \"top\": \"\", " +
	                    "                    \"right\": \"\", " +
	                    "                    \"bottom\": \"\", " +
	                    "                    \"left\": \"\", " +
	                    "                    \"classes\": \"top left\" " +
	                    "                } " +
	                    "            } " +
	                    "        }, { " +
	                    "            \"id\": \"Oskari.mapframework.bundle.mapmodule.plugin.SearchPlugin\", " +
	                    "            \"selected\": false, " +
	                    "            \"lefthanded\": \"top right\", " +
	                    "            \"righthanded\": \"top left\", " +
	                    "            \"config\": { " +
	                    "                \"location\": { " +
	                    "                    \"top\": \"\", " +
	                    "                    \"right\": \"\", " +
	                    "                    \"bottom\": \"\", " +
	                    "                    \"left\": \"\", " +
	                    "                    \"classes\": \"top right\" " +
	                    "                }, " +
	                    "                    \"url\":  \"/action?action_route=GetWfsSearchResult\" " +
	                    "            } " +
	                    "        }, { " +
	                    "            \"id\": \"Oskari.mapframework.mapmodule.ControlsPlugin\", " +
	                    "            \"selected\": true " +
	                    "        }, { " +
	                    "            \"id\": \"Oskari.mapframework.bundle.mapmodule.plugin.PublisherToolbarPlugin\", " +
	                    "            \"selected\": false, " +
	                    "            \"lefthanded\": \"top right\", " +
	                    "            \"righthanded\": \"top left\", " +
	                    "            \"config\": { " +
	                    "                \"location\": { " +
	                    "                    \"top\": \"\", " +
	                    "                    \"right\": \"\", " +
	                    "                    \"bottom\": \"\", " +
	                    "                    \"left\": \"\", " +
	                    "                    \"classes\": \"top right\" " +
	                    "                }, " +
	                    "                \"toolbarId\": \"PublisherToolbar\" " +
	                    "            } " +
	                    "        }, { " +
	                    "            \"id\": \"Oskari.mapframework.mapmodule.GetInfoPlugin\", " +
	                    "            \"selected\": true, " +
	                    "            \"config\": { " +
	                    "                \"ignoredLayerTypes\": [\"WFS\"], " +
	                    "                \"infoBox\": false " +
	                    "            } " +
	                    "        }]");
	            config.put("tools", addTools);
	            return true;
	        }
	        return false;
	    }
}
