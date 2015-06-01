package tampere.actions;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@OskariActionRoute("GetWfsSearchResult")
public class SearchFromWFSChannelActionHandler extends ActionHandler {
	
	private static final Logger log = LogFactory.getLogger(SearchFromWFSChannelActionHandler.class);
    private static final String PARAM_SEARCH_KEY = "searchKey";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_CHANNELIDS_KEY = "channelIds";

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
        
		try {
			channelIds = new JSONArray(params.getHttpParam(PARAM_CHANNELIDS_KEY));
			
			for (int i = 0; i < channelIds.length(); i++) {
						
				List<String> layerIds = new ArrayList<String>();
				layerIds.add(String.valueOf(channelIds.get(i))); 
				JSONObject userLayers = OskariLayerWorker.getListOfMapLayersById(layerIds, params.getUser(), params.getLocale().getLanguage(), false, false);
				JSONArray layers = userLayers.getJSONArray(OskariLayerWorker.KEY_LAYERS);
				log.debug(layers.toString());
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
            sc.addParam("channelIds", channelIds);

            sc.setLocale(locale.getLanguage());

            for(String channelId : channels) {
                sc.addChannel(channelId);
            }
            final JSONObject result = SearchWorker.doSearch(sc);
            ResponseHelper.writeResponse(params, result);
        }
    }
}