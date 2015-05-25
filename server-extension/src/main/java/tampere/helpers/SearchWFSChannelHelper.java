package tampere.helpers;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tampere.domain.WFSSearchChannelsConfiguration;
import tampere.wfs.WFSSearchChannelsConfigurationService;
import tampere.wfs.WFSSearchChannelsConfigurationServiceIbatisImpl;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SearchWFSChannelHelper {
	
	private static final Logger log = LogFactory.getLogger(SearchWFSChannelHelper.class);
	private static WFSSearchChannelsConfigurationService channelService = new WFSSearchChannelsConfigurationServiceIbatisImpl();
	
	/**
	 * Get WFS channels
	 * @return
	 */
	public static JSONObject getChannels() throws JSONException{
		JSONObject job = new JSONObject();
		List<WFSSearchChannelsConfiguration> channels = channelService.findChannels();
		JSONArray channelsJSONArray = new JSONArray();
		
		for(int i=0;i<channels.size();i++){
			WFSSearchChannelsConfiguration channel = channels.get(i);
			JSONObject channelJSON = channel.getAsJSONObject();
			channelsJSONArray.put(channel);
		}
	   	
	   	job.put("channels", channelsJSONArray);
	   	 
	   	return job;
	}
}
