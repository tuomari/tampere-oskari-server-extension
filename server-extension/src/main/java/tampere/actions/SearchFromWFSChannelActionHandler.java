package tampere.actions;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tampere.domain.WFSSearchChannelsConfiguration;
import tampere.helpers.SearchWFSChannelHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@OskariActionRoute("GetWfsSearchResult")
public class SearchFromWFSChannelActionHandler extends ActionHandler {
	
	private static final Logger log = LogFactory.getLogger(SearchFromWFSChannelActionHandler.class);
    private static final String PARAM_SEARCH_KEY = "searchKey";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_CHANNELIDS_KEY = "channelIds";
    private static final String PARAM_LAYERNAME = "layerName";
    private static final String PARAM_SRS = "srs_name";
    private static final String PARAM_VERSION = "version";
    private static final String PARAM_URL = "wmsUrl";
    private static final String PARAM_REALNAME = "name";
    public static final String PARAM_CHANNELS = "channels";
    private static final String PARAM_ADMIN = "admin";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";

    private String[] channels = new String[0];

    public void init() {
        channels = PropertyUtil.getCommaSeparatedList("actionhandler.GetSearchResult.channels");
    }


    public void handleAction(final ActionParameters params) throws ActionException {
        final String search = params.getHttpParam(PARAM_SEARCH_KEY);
        if (search == null) {
            throw new ActionParamsException("Search string was null");
        }
        final String epsg = params.getHttpParam(PARAM_EPSG_KEY);
        
        JSONArray channelIds;
        List<WFSSearchChannelsConfiguration> channelsParams = new ArrayList<WFSSearchChannelsConfiguration>();
        
		try {
			
			channelIds = new JSONArray(params.getHttpParam(PARAM_CHANNELIDS_KEY));
			List<WFSSearchChannelsConfiguration> channels = SearchWFSChannelHelper.getChannelById(channelIds);
			
			for (int i = 0; i < channels.size(); i++) {
				WFSSearchChannelsConfiguration channel = channels.get(i);
				List<String> layerIds = new ArrayList<String>();
				layerIds.add(String.valueOf(channel.getWFSLayerId())); 
				JSONObject userLayers = OskariLayerWorker.getListOfMapLayersById(layerIds, params.getUser(), params.getLocale().getLanguage(), false, false);
				JSONArray layers = userLayers.getJSONArray(OskariLayerWorker.KEY_LAYERS);

				if(layers.length() == 1){
					if(layers.getJSONObject(0).has(PARAM_ADMIN)){
						JSONObject adminJSON = layers.getJSONObject(0).getJSONObject(PARAM_ADMIN);
						channel.setUsername(adminJSON.getString(PARAM_USERNAME));
						channel.setPassword(adminJSON.getString(PARAM_PASSWORD));
					}
					channel.setLayerName(layers.getJSONObject(0).getString(PARAM_LAYERNAME));
					channel.setSrs(layers.getJSONObject(0).getString(PARAM_SRS));
					channel.setVersion(layers.getJSONObject(0).getString(PARAM_VERSION));
					channel.setUrl(layers.getJSONObject(0).getString(PARAM_URL));
					channel.setRealName(layers.getJSONObject(0).getJSONObject(PARAM_REALNAME));
					channelsParams.add(channel);
				}
				
			}
			
		} catch (JSONException ex) {
			 log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex));    		 
	   		 throw new ActionParamsException("Couldn't get WFS search channelsIds");
		}
		
        final String error = SearchWorker.checkLegalSearch(search);

        if (!SearchWorker.STR_TRUE.equals(error)) {
            // write error message key
            ResponseHelper.writeResponse(params, error);
        } else {
            final Locale locale = params.getLocale();

            final SearchCriteria sc = new SearchCriteria();
            sc.setSearchString(search);
            sc.setSRS(epsg);  // eg. EPSG:3067
            sc.addParam(PARAM_CHANNELS, channelsParams);

            sc.setLocale(locale.getLanguage());

            for(String channelId : channels) {
                sc.addChannel(channelId);
            }
            final JSONObject result = SearchWorker.doSearch(sc);
            ResponseHelper.writeResponse(params, result);
        }
    }
}