# tampere-oskari-server-extension

Tampere Oskari server extension

Extends Oskari server functionality to serve WFS search.

## Prerequisites

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
actionhandler.GetAppSetup.dynamic.bundles = admin-layerselector, admin-layerrights, admin, admin-users, admin-wfs-search-channel
actionhandler.GetAppSetup.dynamic.bundle.admin.roles = Admin
actionhandler.GetAppSetup.dynamic.bundle.admin-wfs-search-channel.roles = Admin
search.channel.WFSSEARCH_CHANNEL.maxFeatures = 100
search.channels.default=WFSSEARCH_CHANNEL
actionhandler.GetSearchResult.channels=WFSSEARCH_CHANNEL
```

## Installation

* Clone https://github.com/dimenteq/tampere-oskari-server-extension/tree/develop_server
```Bash
git clone https://github.com/dimenteq/tampere-oskari-server-extension.git
```
* Change develop_server branch
```
cd tampere-oskari-server-extension
git checkout develop_server
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
