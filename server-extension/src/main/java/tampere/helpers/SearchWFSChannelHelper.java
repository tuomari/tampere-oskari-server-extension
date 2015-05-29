package tampere.helpers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tampere.domain.WFSSearchChannelsConfiguration;
import tampere.wfs.WFSSearchChannelsConfigurationService;
import tampere.wfs.WFSSearchChannelsConfigurationServiceIbatisImpl;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SearchWFSChannelHelper {
	
	private static final Logger log = LogFactory.getLogger(SearchWFSChannelHelper.class);
	private static WFSSearchChannelsConfigurationService channelService = new WFSSearchChannelsConfigurationServiceIbatisImpl();
	
	/**
	 * Get WFS channels
	 * @param string 
	 * @param user 
	 * @return
	 */
	public static JSONObject getChannels(User user, String lang) throws JSONException{
		JSONObject job = new JSONObject();
		List<WFSSearchChannelsConfiguration> channels = channelService.findChannels();
		JSONArray channelsJSONArray = new JSONArray();
		
		for(int i=0;i<channels.size();i++){
			WFSSearchChannelsConfiguration channel = channels.get(i);
			JSONObject channelJSON = channel.getAsJSONObject();
			List<String> layerIds = new ArrayList<String>();
			layerIds.add(String.valueOf(channel.getWFSLayerId())); 
			JSONObject userLayers = OskariLayerWorker.getListOfMapLayersById(layerIds, user, lang, false, false);
			JSONArray layers = userLayers.getJSONArray(OskariLayerWorker.KEY_LAYERS);

			if(layers.length() > 0){
				channelsJSONArray.put(channelJSON);
			}
		}
	   	
	   	job.put("channels", channelsJSONArray);
	   	 
	   	return job;
	}
	
	/**
	 * Delete selected channel
	 * @param channelId
	 */
	public static JSONObject delete(final int channelId) {
		JSONObject job = new JSONObject();
		try{
			channelService.delete(channelId);
			job.put("success", true);
		} catch (Exception e) {
			try{
				job.put("success", false);
			} catch (Exception ex) {}
		}
		return job;
	}
	
	/**
	 * Add WFS channel
	 * @param channel
	 */
	public static JSONObject insert(final WFSSearchChannelsConfiguration channel) {
		JSONObject job = new JSONObject();
		try{
			int newId = channelService.insert(channel);
			job.put("success", newId > 0);
		} catch (Exception e) {
			log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			try{
				job.put("success", false);
			} catch (Exception ex) {}
		}
		return job;
	}
	
	/**
	 * Update WFS channel
	 * @param channel
	 */
	public static JSONObject update(final WFSSearchChannelsConfiguration channel) {
		JSONObject job = new JSONObject();
		try{
			channelService.update(channel);
			job.put("success", true);
		} catch (Exception e) {
			log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			try{
				job.put("success", false);
			} catch (Exception ex) {}
		}
		return job;
	}
}
