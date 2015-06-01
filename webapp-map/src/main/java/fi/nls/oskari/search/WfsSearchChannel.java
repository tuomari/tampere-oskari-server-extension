package fi.nls.oskari.search;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.search.channel.SearchChannel;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.log.Logger;

import java.net.URLEncoder;

import fi.nls.oskari.util.PropertyUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

@Oskari(WfsSearchChannel.ID)
public class WfsSearchChannel extends SearchChannel {

    /**
     * logger
     */
    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;
    public static final String ID = "WFSSEARCH_CHANNEL";
    public final static String SERVICE_SRS = "EPSG:3067";

    private static final String PROPERTY_SERVICE_URL = "search.channel.WFSSEARCH_CHANNEL.service.url";

    @Override
    public void init() {
        super.init();
        // set in ext props
        serviceURL = PropertyUtil.get(PROPERTY_SERVICE_URL, "http://nominatim.openstreetmap.org/search");
        log.debug("ServiceURL set to " + serviceURL);
    }

    /**
     * Returns the search raw results.
     *
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    private String getData(SearchCriteria searchCriteria) throws Exception {
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return new String();
        }

        StringBuffer buf = new StringBuffer(serviceURL);
        String searchStr = URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8");
        log.debug("[tre] Search string: " + searchStr);
        log.debug("[tre] Channels: " + searchCriteria.getParam("channelIds").toString());

        if (isKiinteistoTunnus(searchStr)) {
            // urls copied from the browser. tested with urlencoder and UTF-8 - no dice.
           /* buf.append("service=WFS&version=1.0.0%20&request=GetFeature&typeName=tampere_ora:KI_KIINTEISTOT_MVIEW
            &srsName=EPSG:3067&outputformat=json
            &Filter=%3CFilter%3E%20%3CPropertyIsLike%20wildCard=%27*%27%20singleChar=%27.%27%20
            escape=%27!%27%3E%20%3CPropertyName%3EC_TUNNUS_2%3C/PropertyName%3E%20%3CLiteral%3E");*/

            // i/F does not work with short kiinteisto format. Padding needed
            String newSearchStr = convertToLongFormat(searchStr);

            buf.append("service=WFS&version=1.0.0%20" +
                    "&request=GetFeature" +
                    "&typeName=tampere_ora:KI_KIINTEISTOT_MVIEW" +
                    "&srsName=EPSG:3067" +
                    "&outputformat=json" +
                    "&Filter=%3CFilter%3E%20%3CPropertyIsLike%20wildCard=%27*%27%20singleChar=%27.%27%20" +
                    "escape=%27!%27%3E%20%3CPropertyName%3EC_TUNNUS_2%3C/PropertyName%3E%20%3CLiteral%3E*");

            buf.append(URLEncoder.encode(newSearchStr, "UTF-8"));
            buf.append("*%3C/Literal%3E%20%3C/PropertyIsLike%3E%20%3C/Filter%3E");
            log.debug("[tre] Search string was interpreted as property id (kiinteistotunnus)");
        } else if (isRakennusLupa(searchStr)) {
            /*http://80.248.161.119/tampere_wfs_geoserver/tampere_ora/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=tampere_ora:RAKENNUSLUVAT_VIEW */
            buf.append("service=WFS&version=2.0.0%20" +
                    "&request=GetFeature" +
                    "&typeName=tampere_ora:RAKENNUSLUVAT_VIEW" +
                    "&srsName=EPSG:3067" +
                    "&outputformat=json" +
                    "&Filter=%3CFilter%3E%20%3CPropertyIsLike%20wildCard=%27*%27%20singleChar=%27.%27%20" +
                    "escape=%27!%27%3E%20%3CPropertyName%3EC_LUPATUNNUS%3C/PropertyName%3E%20%3CLiteral%3E*");

            String trimmedSearchStr = searchStr.toLowerCase().replace("%23rl", "").replace("+", ""); // remove tags
            buf.append(URLEncoder.encode(trimmedSearchStr.trim(), "UTF-8"));
            log.debug("[tre] trimmedSearchStr:" + trimmedSearchStr);
            buf.append("*%3C/Literal%3E%20%3C/PropertyIsLike%3E%20%3C/Filter%3E");
            log.debug("[tre] Search string was interpreted as building permit (rakennuslupa)");
        }
        else {
            log.debug("[tre] Search string was interpreted as address");
            String streetName = searchStr;
            String streetNumber = "";
            // find last word and if it is number then it must be street number?
            String lastWord = searchStr.substring(searchStr.lastIndexOf("+") + 1);

            if (isStreetNumber(lastWord)) {
                // override streetName without, street number
                streetName = searchStr.substring(0, searchStr.lastIndexOf("+"));
                log.debug("[tre] found streetnumber" + streetName);
                streetNumber = lastWord;
            }

            log.debug("[tre] StreetName: " + streetName + " StreetNumber: " + streetNumber);

            /*http://tampere.navici.com/tampere_wfs_geoserver/tampere_ora/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=tampere_ora:ONK_NMR_MVIEW&srsName=EPSG:3067&outputformat=json&Filter=%3CFilter%3E%3CPropertyIsLike%20wildCard=%22*%22%20singleChar=%22.%22%20escape=%22!%22%3E%3CPropertyName%3EKADUNNIMI%3C/PropertyName%3E%3CLiteral%3EOllinojan*%3C/Literal%3E%3C/PropertyIsLike%3E%3C/Filter%3E*/
            // this is with street name is somewhat as search string
            buf.append("service=WFS&version=2.0.0&request=GetFeature&typeName=tampere_ora:ONK_NMR_MVIEW&srsName=EPSG:3067" +
                    "&outputformat=json&Filter=%3CFilter%3E%3CAnd%3E%3CPropertyIsLike%20" +
                    "wildCard=%22*%22%20singleChar=%22.%22%20" +
                    "escape=%22!%22%3E%3CPropertyName%3EKADUNNIMI%3C/PropertyName%3E%3CLiteral%3E");
            buf.append(streetName.trim());
            buf.append("*%3C/Literal%3E%3C/PropertyIsLike%3E%3CPropertyIsLike%20" +
                    "wildCard=%22*%22%20singleChar=%22.%22%20escape=%22!%22%3E" +
                    "%3CPropertyName%3ENUMERO%3C/PropertyName%3E%3CLiteral%3E");
            buf.append(streetNumber);
            buf.append("*%3C/Literal%3E%3C/PropertyIsLike%3E%3C/And%3E%3C/Filter%3E");
        }

        log.debug("[tre] search path:" + buf.toString());
        String data = IOHelper.readString(getConnection(buf.toString()));
        log.debug("[tre] DATA: " + data);

        return data;
    }

    /**
     * Returns the search raw results.
     *
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    private String getDataNimisto(SearchCriteria searchCriteria) throws Exception {
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return new String();
        }

        StringBuffer buf = new StringBuffer(serviceURL);
        String searchStr = URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8");
        log.debug("[tre] Search Nimisto string: " + searchStr);

        buf.append("service=WFS&version=2.0.0&request=GetFeature&typeName=tampere_ora:NIMISTO_MVIEW&srsName=EPSG:3067" +
                "&outputformat=json&Filter=%3CFilter%3E%3CPropertyIsLike%20" +
                "wildCard=%22*%22%20singleChar=%22.%22%20" +
                "escape=%22!%22%3E%3CPropertyName%3ENIMI%3C/PropertyName%3E%3CLiteral%3E");
        buf.append(searchStr.trim());
        buf.append("*%3C/Literal%3E%3C/PropertyIsLike%3E%3C/Filter%3E");

        log.debug("[tre] search Nimisto path:" + buf.toString());
        String data = IOHelper.readString(getConnection(buf.toString()));
        log.debug("[tre] DATA2: " + data);

        return data;
    }


    /**
     * Returns the channel search results.
     *
     * @param searchCriteria Search criteria.
     * @return Search results.
     */
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        ChannelSearchResult searchResultList = new ChannelSearchResult();
        String queryStr = searchCriteria.getSearchString();
        log.debug("[tre] doSearch queryStr: " + queryStr);

        try {
            final JSONObject resp = new JSONObject(getData(searchCriteria));

            if (isKiinteistoTunnus(queryStr)) {
                JSONArray featuresArr = resp.getJSONArray("features");

                for (int i = 0; i < featuresArr.length(); i++) {
                    SearchResultItem item = new SearchResultItem();

                    JSONObject loopJSONObject = featuresArr.getJSONObject(i);

                    JSONObject objProperties = loopJSONObject.getJSONObject("properties");
                    item.setType(objProperties.getString("AINEISTO"));
                    item.setTitle(objProperties.getString("C_TUNNUS_2"));

                    JSONObject featuresObj_geometry = loopJSONObject.getJSONObject("geometry");
                    JSONArray coordsArray = featuresObj_geometry.getJSONArray("coordinates");
                    item.setLon("" + coordsArray.getInt(0));
                    item.setLat("" + coordsArray.getInt(1));

                    item.setVillage("Tampere");
                    item.setDescription("");
                    item.setLocationTypeCode("");

                    searchResultList.addItem(item);
                }
            } else if (isRakennusLupa(queryStr)) {
                JSONArray featuresArr = resp.getJSONArray("features");

                for (int i = 0; i < featuresArr.length(); i++) {
                    SearchResultItem item = new SearchResultItem();

                    JSONObject loopJSONObject = featuresArr.getJSONObject(i);

                    JSONObject objProperties = loopJSONObject.getJSONObject("properties");
                    item.setType(objProperties.getString("C_TUNNUSTYYPPI"));
                    item.setTitle(objProperties.getString("C_LUPATUNNUS"));

                    JSONObject featuresObj_geometry = loopJSONObject.getJSONObject("geometry");
                    JSONArray coordsArray = featuresObj_geometry.getJSONArray("coordinates");
                    item.setLon("" + coordsArray.getInt(0));
                    item.setLat("" + coordsArray.getInt(1));

                    item.setVillage("Tampere");
                    item.setDescription("");
                    item.setLocationTypeCode("");

                    searchResultList.addItem(item);
                }
            } else// parse address information
            {
                JSONArray featuresArr = resp.getJSONArray("features");

                for (int i = 0; i < featuresArr.length(); i++) {
                    SearchResultItem item = new SearchResultItem();

                    JSONObject loopJSONObject = featuresArr.getJSONObject(i);

                    JSONObject objProperties = loopJSONObject.getJSONObject("properties");
                    item.setTitle(objProperties.getString("KADUNNIMI") + " " + objProperties.getString("NUMERO"));
                    item.setType("Katuosoite");

                    JSONObject featuresObj_geometry = loopJSONObject.getJSONObject("geometry");
                    JSONArray coordsArray = featuresObj_geometry.getJSONArray("coordinates");
                    item.setLon("" + coordsArray.getInt(0));
                    item.setLat("" + coordsArray.getInt(1));

                    item.setVillage("Tampere");
                    item.setDescription("");
                    item.setLocationTypeCode("");

                    searchResultList.addItem(item);
                }

                // Nimisto 
                final JSONObject respNimisto = new JSONObject(getDataNimisto(searchCriteria));
                JSONArray featuresArrNimisto = respNimisto.getJSONArray("features");

                for (int i = 0; i < featuresArrNimisto.length(); i++) {
                    SearchResultItem itemNimisto = new SearchResultItem();

                    JSONObject loopJSONObjectNimisto = featuresArrNimisto.getJSONObject(i);

                    JSONObject objProperties2 = loopJSONObjectNimisto.getJSONObject("properties");
                    itemNimisto.setTitle(objProperties2.getString("NIMI"));
                    itemNimisto.setType(objProperties2.getString("TYYPPI"));

                    JSONObject featuresObj_geometry2 = loopJSONObjectNimisto.getJSONObject("geometry");
                    JSONArray coordsArray2 = featuresObj_geometry2.getJSONArray("coordinates");
                    itemNimisto.setLon("" + coordsArray2.getInt(0));
                    itemNimisto.setLat("" + coordsArray2.getInt(1));

                    itemNimisto.setVillage("Tampere");
                    itemNimisto.setDescription("");
                    itemNimisto.setLocationTypeCode("");

                    searchResultList.addItem(itemNimisto);
                }

            }

        } catch (Exception e) {
            log.error(e, "[tre] Failed to search locations from register of TampereChannel");
        }
        return searchResultList;
    }

    /**
     * Returns the true if the kiinteisto tunnus is set as search criteria.
     *
     * @param searchCriteria Search criteria.
     * @return true if string is numeric. Also hyphen is allowed.
     */
    private boolean isKiinteistoTunnus(String test) {
        //return test.matches("[0-9-*]+");
        return test.matches("[m,v,M,V0-9-*]+");
    }

    /**
     * Returns the true if test contains numbers and/or a/b.
     *
     * @param searchCriteria Search criteria.
     * @return true if string can be set to street number field in wfs query.
     */
    private boolean isStreetNumber(String test) {
        log.debug("[tre] street number candidate: " + test);
        return test.matches("[0-9-a-b]+");
    }

    /**
     * Returns the true if test contains numbers prefix #rl and/or without whitespace + digits .
     *
     * @param searchCriteria Search criteria.
     * @return true if string can be set to rakennnuslupa field in wfs query.
     */
    private boolean isRakennusLupa(String test) {
        log.debug("[tre] rakennuslupa candidate: " + test);
        String temp = test.toLowerCase();
        return temp.contains("rl");
    }

    /**
     * Returns padded kiinteistötunnus if the tunnus was in fully qualified short format.
     *
     * @param searchStr Search criteria.
     * @return Kiinteistotunnus with padding e.g. long format.
     */
    private String convertToLongFormat(String searchStr) {
        log.debug("[tre] convert candidate: " + searchStr);
        String[] elements = searchStr.split("-");
        String res = "";

        // 837-1-4809-10 must have four elements
        if (elements.length == 4) {
            String city = elements[0];                                                // 837 Tampere no padding needed
            String district = ("000" + elements[1]).substring(elements[1].length());  // 3 digits - 001 or 1
            String block = ("0000" + elements[2]).substring(elements[2].length());    // 4 digits - 0001 or 1
            String lot = ("0000" + elements[3]).substring(elements[3].length());      // 4 digits - 0001 or 1

            res = new StringBuilder()
                    .append(city)
                    .append("-")
                    .append(district)
                    .append("-")
                    .append(block)
                    .append("-")
                    .append(lot).toString();
        }
        else {
            res = searchStr; // nothing to be done
        }
        log.debug("[tre] convert result: " + res);
        return res;
    }
}
/*837-001-4809-0010 long version
  837-1-4809-10     short version*/
/*{
  "type": "FeatureCollection",
  "totalFeatures": 1,
  "features": [
    {
      "type": "Feature",
      "id": "KI_KIINTEISTOT_MVIEW.fid-7882b2c6_14bc5ccbb66_4ac7",
      "geometry": {
        "type": "Point",
        "coordinates": [
          325612.63836099283,
          6819645.1078560855
        ]
      },
      "geometry_name": "GEOLOC",
      "properties": {
        "AINEISTO": "KiinteistÃ¶",
        "C_KUNTA": "837",
        "C_SIJAINTI": "301",
        "C_RYHMA": "0932",
        "C_YKSIKKO": "0006",
        "C_TUNNUS_2": "837-301-0932-0006",
        "I_XKOORD": 6818024,
        "I_YKOORD": 24485404,
        "DOKUMENTIT": "<a href=\"http://paikkatietopalvelu.tampere.fi/solr/?q=837-301-0932-0006\">837-301-0932-0006<\/a>"
      }
    }
  ],
} */
