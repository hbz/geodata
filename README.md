# lobid-geo-enrichment
Microservice for geo information enrichment

This microservice allows to obtain geo data from "Nominatim":http://www.nominatim.org/.
To prevent data from being reloaded repeatedly, once obtained geo data is stored in an Elasticsearch index that should be part of the local machine or the local area network.

Currently, the service provides information for
* Latitude
* Longitude
* Post code
