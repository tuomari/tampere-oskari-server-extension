package tampere.domain;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;

public class WFSSearchChannelsConfiguration {
	
	private static final Logger log = LogFactory.getLogger(WFSSearchChannelsConfiguration.class);
	
	protected final static String PARAM_ID = "id";
    protected final static String PARAM_WFS_LAYER_ID = "wfsId";
    protected final static String PARAM_TOPIC = "topic";
    protected final static String PARAM_DESC = "desc";
    protected final static String PARAM_PARAMS_FOR_SEARCH = "params_for_search";
	
	
	private int id = -1;
	private String WFSLayerId;	
	private JSONObject topic;
	private JSONObject desc;
	private JSONArray paramsForSearch;
	
	
	public JSONObject getAsJSONObject() {
		final JSONObject root = new JSONObject();
		JSONHelper.putValue(root, PARAM_ID, this.getId());
		JSONHelper.putValue(root, PARAM_WFS_LAYER_ID, this.getWFSLayerId());
		JSONHelper.putValue(root, PARAM_TOPIC, this.getTopic());
		JSONHelper.putValue(root, PARAM_DESC, this.getDesc());
		JSONHelper.putValue(root, PARAM_PARAMS_FOR_SEARCH, this.getParamsForSearch());		
		return root;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getWFSLayerId() {
		return WFSLayerId;
	}


	public void setWFSLayerId(String wFSLayerId) {
		WFSLayerId = wFSLayerId;
	}


	public JSONObject getTopic() {
		return topic;
	}


	public void setTopic(JSONObject topic) {
		this.topic = topic;
	}


	public JSONObject getDesc() {
		return desc;
	}


	public void setDesc(JSONObject desc) {
		this.desc = desc;
	}


	public JSONArray getParamsForSearch() {
		return paramsForSearch;
	}


	public void setParamsForSearch(JSONArray paramsForSearch) {
		this.paramsForSearch = paramsForSearch;
	}

}
