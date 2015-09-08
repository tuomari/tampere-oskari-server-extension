# tampere-oskari-server-extension

Tampere Oskari server extension

Extends Oskari server functionality to serve WFS search.

## Prerequisites

These uses Oskari 1.31.1 version.

### Front-end

This extension needs new front-end codes of Oskari (see tampere bundles in https://github.com/dimenteq/tampere-oskari).

### Back-end

This version upgrade drops all ready defined search channels (database upgrades has moved to Flyway).

##### Database

Nothing need to be done. This application upgrades it's database automatically.

##### oskari-ext.properties file changes

```Shell
actionhandler.GetAppSetup.dynamic.bundles = admin-layerselector, admin-layerrights, admin, admin-users, admin-wfs-search-channel
actionhandler.GetAppSetup.dynamic.bundle.admin.roles = Admin
actionhandler.GetAppSetup.dynamic.bundle.admin-wfs-search-channel.roles = Admin
search.channel.WFSSEARCH_CHANNEL.service.url= [URL_FOR_SERVICE]
search.channel.WFSSEARCH_CHANNEL.maxFeatures = 100
search.channels.default=WFSSEARCH_CHANNEL
actionhandler.GetSearchResult.channels=WFSSEARCH_CHANNEL
db.additional.modules=tampere
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

* If you see following error "Could not resolve dependencies for project fi.nls.oskari.spring:webapp.map:w
ar:1.2.0-SNAPSHOT: Could not find artifact", fix this to edit again oskari-spring/webapp-spring/pom.xml file (remove line above):
```Bash
# Remove following line
<version>1.2.0-SNAPSHOT</version>
```
* Stop Jetty
* Remove Oskari spring-map folder if exists (github clone)
* Remove <JETTY>/webapps/spring-map.war
* Remove <JETTY>/webapp/transport.war
* Rename from <JETTY>/contexts/spring-map.xml to <JETTY>/contexts/oskari-map.xml
* Edit <JETTY>/contexts/oskari-map.xml -file and change /webapps/spring-map.war to /webapps/oskari-map.war
* Copy oskari-server-extension/webapp-map/target/oskari-map.war to <JETTY>/webapps/oskari-map.war
* Copy oskari-server-extension/webapp-transport/target/transport.war to <JETTY>/webapps/transport.war
* Start Jetty
