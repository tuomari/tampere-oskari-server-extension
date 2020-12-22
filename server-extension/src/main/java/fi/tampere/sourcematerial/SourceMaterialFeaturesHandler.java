package fi.tampere.sourcematerial;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.feature.GetWFSFeaturesHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

@OskariActionRoute("SourceMaterialFeatures")
public class SourceMaterialFeaturesHandler extends GetWFSFeaturesHandler {

    private static final Logger LOG = LogFactory.getLogger(SourceMaterial.class);

    protected OskariLayer findMapLayer(String id, User user) throws ActionException {
        int layerId;
        try {
            layerId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new ActionParamsException(ERR_INVALID_ID);
        }
        OskariLayer layer = permissionHelper.getLayer(layerId, user);
        if (OskariLayer.TYPE_WFS.equals(layer.getType())) {
            return layer;
        }
        /*
        if (OskariLayer.TYPE_WMS.equals(layer.getType())) {
            throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
        }
        // TODO: create WFSLayer from WMS
        LOG.warn(layer);
        return layer;
         */
        // TODO: remove this once the commented out code is ready to map wms -> wfs
        //  only used to mimic current impl for drop-in replacement
        throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
    }
}
