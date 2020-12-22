package fi.tampere.sourcematerial;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.feature.GetWFSFeaturesHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

import java.util.HashMap;
import java.util.Map;

@OskariActionRoute("SourceMaterialFeatures")
public class SourceMaterialFeaturesHandler extends GetWFSFeaturesHandler {

    private static final Logger LOG = LogFactory.getLogger(SourceMaterial.class);

    @Override
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
        if (!OskariLayer.TYPE_WMS.equals(layer.getType())) {
            throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
        }
        // TODO: create WFSLayer from WMS
        OskariLayer mock = new OskariLayer();
        String url = layer.getUrl().trim();
        // crude approach to change wms -> wfs
        if (url.endsWith("?")) {
            url = url.substring(0, url.length() -1);
        }
        if (url.endsWith("/wms")) {
            // TODO: replace with call to DescribeLayer to get the url
            // -> cache url parsed from response with key == layerId, value = url from Describe
            mock.setUrl(url.substring(0, url.length() - 5) + "/wfs?");
        } else if (url.endsWith("/ows")) {
            // TODO: replace with call to DescribeLayer to get the url
            // -> cache url parsed from response with key == layerId, value = url from Describe
            mock.setUrl(url.substring(0, url.length() - 5) + "/wfs?");
        } else {
            throw new ActionParamsException("Not implemented yet: " + url);
        }
        mock.setType(OskariLayer.TYPE_WFS);
        mock.setName(layer.getName());
        mock.setVersion("1.1.0");
        LOG.debug("Original layer", layer.getUrl(), layer.getName());
        LOG.debug("WFS mock", mock.getUrl(), mock.getName());
        //LOG.debug("WFS mock", mock);
        return mock;
        // TODO: remove this once the commented out code is ready to map wms -> wfs
        //  only used to mimic current impl for drop-in replacement
        //throw new ActionParamsException(ERR_LAYER_TYPE_NOT_WFS);
    }

    private String getDescribeLayerUrl(OskariLayer layer) {
        Map<String, String> params = new HashMap<>();
        params.put("service", "WMS");
        params.put("request", "DescribeLayer");
        // server only supports 1.1.1
        params.put("version", "1.1.1");
        params.put("LAYERS", layer.getName());

        return IOHelper.constructUrl(layer.getUrl(), params);
    }

    /*
    https://geodata.tampere.fi/geoserver/joukkoliikenne/wms?
      service=WMS&version=1.1.1&request=DescribeLayer&LAYERS=raitiotiepysakit_keskusta_hervanta_tays

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE WMS_DescribeLayerResponse SYSTEM
    "https://geodata.tampere.fi/geoserver/schemas/wms/1.1.1/WMS_DescribeLayerResponse.dtd">
<WMS_DescribeLayerResponse version="1.1.1">
  <LayerDescription name="raitiotiepysakit_keskusta_hervanta_tays"
        wfs="https://geodata.tampere.fi/geoserver/joukkoliikenne/wfs?"
        owsURL="https://geodata.tampere.fi/geoserver/joukkoliikenne/wfs?" owsType="WFS">
    <Query typeName="joukkoliikenne:raitiotiepysakit_keskusta_hervanta_tays"/>
  </LayerDescription>
</WMS_DescribeLayerResponse>

     */
}
