# tampere-oskari-server-extension
Tampere Oskari server extension

Extends Oskari server functionality to serve WFS searchs.

## Usage

1. Clone https://github.com/dimenteq/tampere-oskari-server-extension/tree/develop_server
2. Run **mvn clean install** in tampere-oskari-server-extension folder
3. Clone https://github.com/nls-oskari/oskari-spring
4. Add tampere-oskari-server-extension dependency to oskari-spring/webapp-spring/pom.xml: 
5. ```Xml
6. <dependency>
7. <groupId>fi.tampere.oskari</groupId>
8. <artifactId>server-extension</artifactId>
9. <version>1.0-SNAPSHOT</version>
10. </dependency>
11. ```
5. Run **mvn clean install** in oskari-spring folder
6. Copy oskari-spring/webapp-spring/target/spring-map.war to your Jetty installation webapps folder

## Dependencies

### Front-end
This extension needs new front-end codes of Oskari (see tampere bundles in https://github.com/dimenteq/tampere-oskari).

### Back-end
#### New table and sequence
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

CREATE SEQUENCE oskari_wfs_search_channel_seq
  INCREMENT 1
```
#### oskari-ext.properties file changes
```Shell
search.channel.WFSSEARCH_CHANNEL.maxFeatures = 100
search.channels.default=WFSSEARCH_CHANNEL
actionhandler.GetSearchResult.channels=WFSSEARCH_CHANNEL
```
