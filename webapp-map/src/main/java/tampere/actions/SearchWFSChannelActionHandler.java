package tampere.actions;

import tampere.helpers.SearchWFSChannelHelper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("SearchWFSChannel")
public class SearchWFSChannelActionHandler extends RestActionHandler {
	
	 private static final Logger log = LogFactory.getLogger(SearchWFSChannelActionHandler.class);
     private static final String PARAM_ID = "id";
     
     @Override
     public void handleGet(ActionParameters params) throws ActionException {
    	 // TODO GET CHANNELS

    	 // Only admin user
    	 params.requireAdminUser();
    	 
    	 try {
    		 ResponseHelper.writeResponse(params, SearchWFSChannelHelper.getChannels());
    	 } catch (Exception ex){
    		 log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex));
    		 
    		 throw new ActionParamsException("Couldn't get WFS search channels");
    	 }
     }
     
     @Override
     public void handleDelete(ActionParameters params) throws ActionException {
    	 //  TODO DELETE CHANNEL
    	 
    	 // Only admin user
    	 params.requireAdminUser();
     }
     
     @Override
     public void handlePost(ActionParameters params) throws ActionException {
    	 // TODO UPDATE CHANNEL
    	 
    	 // Only admin user
    	 params.requireAdminUser();
     }
     
     @Override
     public void handlePut(ActionParameters params) throws ActionException {
    	 // TODO SAVE CHANNEL
    	 
    	// Only admin user
    	params.requireAdminUser();
     
     }
     
     
     /*
	 public void handleAction(ActionParameters params) throws ActionException {
		 final int id = ConversionHelper.getInt(params.getRequiredParam(PARAM_ID), -1);
		 
		 if(params.getUser().isAdmin()) {}
		 else {
			 
		 }
		 
	 }
	 */

}
