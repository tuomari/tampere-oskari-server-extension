package tampere.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchWFSChannelHelper {
	
	/**
	 * Get WFS channels
	 * @return
	 */
	public static JSONObject getChannels() throws JSONException{
		JSONObject job = new JSONObject();
   	 	JSONArray channels = new JSONArray();
		
		for(int i=0;i<10;i++) {	   	
			JSONObject channel = new JSONObject();
			channel.put("id", i);
			channel.put("choose-wfs-layer", 1);
			channel.put("details-topic-fi", "Testi " + i + " topic");
			channel.put("details-desc-fi", "Testi " + i + " description");	   	
			channel.put("details-topic-en", "Testi " + i + " topic - en");
			channel.put("details-desc-en", "Testi " + i + " description - en");	   	
			channel.put("details-topic-sv", "Testi " + i + " topic - sv");
			channel.put("details-desc-sv", "Testi " + i + " description -sv");	   	
			JSONArray channelParams = new JSONArray();
			channelParams.put(1);
			channelParams.put(2);
			channel.put("params", channelParams);
			channels.put(channel);
		}
	   	
	   	job.put("channels", channels);
	   	 
	   	return job;
	}
}
