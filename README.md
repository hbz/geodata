# lobid-geo-enrichment

## Overview

Microservice for geo information enrichment.

This microservice allows to obtain geo data from [Nominatim](http://www.nominatim.org/) and [Wikidata](https://www.wikidata.org).
To prevent data from being reloaded repeatedly, once obtained geo data is stored in an Elasticsearch index that should be part of the local machine or the local area network.

Currently, the service provides information for
* Latitude
* Longitude
* Post code

The application is built using the Playframework and Elasticsearch.

## Setup

Create and change into a directory where you want to store the project, e.g.:

``mkdir ~/git ; cd ~/git``

Get the project from GitHub:

``git clone https://github.com/hbz/lobid-organisations.git``

Download activator into your home directory in order to launch the Play app:

``cd ~ ; wget http://downloads.typesafe.com/typesafe-activator/1.3.10/typesafe-activator-1.3.10-minimal.zip``

Start the app:

``~/activator-1.3.10-minimal/bin/activator "start 7401"``

When startup is complete (Listening for HTTP on /0.0.0.0:7401), exit with Ctrl+D, output will be logged to target/universal/stage/logs/application.log.


## Index

The service runs with an embedded Elasticsearch index. The index is created on startup of the application BUT ONLY if it does not yet exist. Note that the index is created incrementally based on the queries the application receives. Additionally, the Nominatim web service restricts the use of the API to one request per second (see http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy). Thus, it takes a considerable amount of time to rebuild the index. If you wish to build the index again, delete the `data` folder in the project root directory and start the application.

## Eclipse

If you'd like to import the project into eclipse, use the activator command `eclipse` to prepare the project:
* Change into the project directory, e.g. `cd ~/git`
* Run ``~/activator-1.3.10-minimal/bin/activator eclipse``
* Import the project into your Eclipse, [like this](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-importproject.htm))
