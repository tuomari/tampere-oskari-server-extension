# tampere-oskari-server-extension

Tampere Oskari server extension

Extends Oskari server functionality to serve WFS search.

## Prerequisites

### Front-end

This extension needs new front-end codes of Oskari (see tampere bundles in https://github.com/dimenteq/tampere-oskari).

### Back-end

These are runned only once in the begin of installation.

#### New table and sequence
```PLpgSQL
CREATE SEQUENCE oskari_wfs_search_channel_seq
  INCREMENT 1;
  
CREATE TABLE oskari_wfs_search_channels
(
  id integer NOT NULL DEFAULT nextval('oskari_wfs_search_channel_seq'::regclass),
  wfs_layer_id integer NOT NULL,
  topic character varying(4000) NOT NULL,
  description character varying(4000),
  params_for_search character varying(4000) NOT NULL,
  is_default boolean,
  is_address boolean,
  CONSTRAINT portti_wfs_search_channels_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE oskari_wfs_search_channels
  OWNER TO postgres;
```

#### Add new bundles
```PLpgSQL
INSERT INTO portti_bundle values((select max(id)+1 from portti_bundle),'admin-wfs-search-channel','{}','{}',' {
	"instanceProps": {
		
	},
	"title": "AdminWfsSearchChannel",
	"bundleinstancename": "admin-wfs-search-channel",
	"fi": "admin-wfs-search-channel",
	"sv": "admin-wfs-search-channel",
	"en": "admin-wfs-search-channel",
	"bundlename": "admin-wfs-search-channel",
	"metadata": {
		"Import-Bundle": {
			"admin-wfs-search-channel": {
				"bundlePath": "/Oskari/packages/tampere/bundle/"
			}
		},
		"Require-Bundle-Instance": []
	}
}');

INSERT INTO portti_bundle values((select max(id)+1 from portti_bundle),'search-from-channels','{}','{}',' {
	"instanceProps": {
		
	},
	"title": "SearchFromChannelsBundle",
	"bundleinstancename": "search-from-channels",
	"fi": "search-from-channels",
	"sv": "search-from-channels",
	"en": "search-from-channels",
	"bundlename": "search-from-channels",
	"metadata": {
		"Import-Bundle": {
			"search-from-channels": {
				"bundlePath": "/Oskari/packages/tampere/bundle/"
			}
		},
		"Require-Bundle-Instance": []
	}
}');
```

### Add bundle to view (check view)

```PLpgSQL
INSERT 
INTO portti_view_bundle_seq
(
	view_id,
	bundle_id,
	seqno,
	config,
	state,
	startup,
	bundleinstance
)
VALUES (
	1,
	(SELECT id FROM portti_bundle WHERE name='search-from-channels'),
	(SELECT max(seqno)+1 FROM portti_view_bundle_seq WHERE view_id=1),
	(SELECT config FROM portti_bundle WHERE name='search-from-channels'),
	(SELECT state FROM portti_bundle WHERE name='search-from-channels'),
	(SELECT startup FROM portti_bundle WHERE name='search-from-channels'),
	'search-from-channels'
);
```

### Add VectorLayer plugin to mapfull bundle (check SQL to respond you database defination)
```PLpgSQL
UPDATE portti_view_bundle_seq SET config='
{ 
     "globalMapAjaxUrl": "[REPLACED BY HANDLER]", 
     "imageLocation": "/Oskari/resources", 
     "mapOptions" : {"srsName":"EPSG:3067","maxExtent":{"bottom":6291456,"left":-548576,"right":1548576,"top":8388608},"resolutions":[2048,1024,512,256,128,64,32,16,8,4,2,1,0.5,0.25,0.125,0.0625]}, 
     "plugins" : [ 
        { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin" }, 
        { "id" : "Oskari.mapframework.mapmodule.WmsLayerPlugin" }, 
        { "id" : "Oskari.mapframework.mapmodule.MarkersPlugin" }, 
        { "id" : "Oskari.mapframework.mapmodule.ControlsPlugin" }, 
        { "id" : "Oskari.mapframework.mapmodule.GetInfoPlugin", 
          "config" : {  
             "ignoredLayerTypes" : ["WFS","MYPLACES", "USERLAYER"], 
             "infoBox": false  
          } 
        }, 
        { "id" : "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin",  
         "config" : {
		"contextPath" : "/transport",
		"hostname" : "localhost",
		"port" : "9901"
         } 
        }, 
        { "id" : "Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin" } , 
        { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.ScaleBarPlugin" }, 
        { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar" }, 
        { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.PanButtons" }, 
        { "id" : "Oskari.mapframework.bundle.mapmyplaces.plugin.MyPlacesLayerPlugin" }, 
        { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.RealtimePlugin" }, 
        { "id" : "Oskari.mapframework.bundle.mapmodule.plugin.FullScreenPlugin" }, 
        { 
             "id" : "Oskari.mapframework.bundle.mapmodule.plugin.BackgroundLayerSelectionPlugin", 
             "config" : { 
                 "showAsDropdown" : false, 
                 "baseLayers" : ["4", "5", "6", "18", "153", "154", "156"] 
             } 
        }, 
        {"id": "Oskari.mapframework.bundle.myplacesimport.plugin.UserLayersLayerPlugin" }, 
        { "id" : "Oskari.arcgis.bundle.maparcgis.plugin.ArcGisLayerPlugin" },
				{ "id" : "Oskari.mapframework.mapmodule.VectorLayerPlugin" }
       ], 
       "layers": [ 
       ] 
 }'
WHERE bundle_id=(SELECT id FROM portti_bundle WHERE name='mapfull') AND view_id=1;
```

#### hide original "Paikkahaku" tab
UPDATE portti_view_bundle_seq SET config='{"disableDefault": true}'  WHERE bundle_id=(SELECT id FROM portti_bundle WHERE name='search') AND view_id = 1;

#### oskari-ext.properties file changes

```Shell
actionhandler.GetAppSetup.dynamic.bundles = admin-layerselector, admin-layerrights, admin, admin-users, admin-wfs-search-channel
actionhandler.GetAppSetup.dynamic.bundle.admin.roles = Admin
actionhandler.GetAppSetup.dynamic.bundle.admin-wfs-search-channel.roles = Admin
search.channel.WFSSEARCH_CHANNEL.service.url= [URL_FOR_SERVICE]
search.channel.WFSSEARCH_CHANNEL.maxFeatures = 100
search.channels.default=WFSSEARCH_CHANNEL
actionhandler.GetSearchResult.channels=WFSSEARCH_CHANNEL
```

## Installation

* Clone https://github.com/dimenteq/tampere-oskari-server-extension/tree/develop
```Bash
git clone https://github.com/dimenteq/tampere-oskari-server-extension.git
```
* Change develop branch
```
cd tampere-oskari-server-extension
git checkout develop
```
* Run mvn clean install in tampere-oskari-server-extension folder
```Bash
mvn clean install
```
* Clone https://github.com/nls-oskari/oskari-spring
```Bash
cd ..
git clone https://github.com/nls-oskari/oskari-spring.git
```
* Change master branch
```Bash
cd oskari-spring
git checkout master
```
* Add tampere-oskari-server-extension dependency to oskari-spring/webapp-spring/pom.xml (edit file): 
```Xml
<dependency>
    <groupId>fi.tampere.oskari</groupId>
    <artifactId>server-extension</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
* Run mvn clean install in oskari-spring folder
```Bash
mvn clean install
```
* If you see following error "Could not resolve dependencies for project fi.nls.oskari.spring:webapp.map:w
ar:1.2.0-SNAPSHOT: Could not find artifact", fix this to edit again oskari-spring/webapp-spring/pom.xml file (remove line above):
```Bash
# Remove following line
<version>1.2.0-SNAPSHOT</version>
```
* Stop Jetty
* Copy oskari-spring/webapp-spring/target/spring-map.war to your Jetty installation webapps folder (and rename war file if you want)
* Start Jetty
