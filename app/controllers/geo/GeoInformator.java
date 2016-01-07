package controllers.geo;

import java.io.IOException;

import org.elasticsearch.action.search.SearchResponse;
import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;

public class GeoInformator extends Controller {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	// for production
	public GeoInformator() {
	}

	public static Result getLatAndLong(String query) throws JSONException, IOException {
		JsonNode latLong = getLatLong(query);
		if (latLong == null) {
			return notFound(Constants.NOT_FOUND.concat(query));
		}
		return ok(
				latLong.get("latitude").asText().concat(Constants.SEPARATOR).concat(latLong.get("longitude").asText()));
	}

	public static Result getPostCodeExplicitNr(String street, String number, String city, String country)
			throws JSONException, IOException {
		return getPostCode(street + " " + number, city, country);
	}

	public static Result getLatExplicitNr(String street, String number, String city, String country)
			throws JSONException, IOException {
		return getLat(street + " " + number, city, country);
	}

	public static Result getLongExplicitNr(String street, String number, String city, String country)
			throws JSONException, IOException {
		return getLong(street + " " + number, city, country);
	}

	public static Result getPostCode(String street, String city, String country) throws JSONException, IOException {
		JsonNode postCode = getPostalCode(street, city, country);
		if (postCode == null) {
			return notFound(Constants.NOT_FOUND.concat(street).concat("+").concat(city).concat("+").concat(country));
		}
		return ok(postCode.asText());
	}

	public static Result getLat(final String street, final String city, final String country)
			throws JSONException, IOException {
		JsonNode latLong = getLatLong(street, city, country);
		if (latLong == null) {
			return notFound(Constants.NOT_FOUND.concat(street).concat("+").concat(city).concat("+").concat(country));
		}
		return ok(latLong.get("latitude").asText());
	}

	public static Result getLong(final String street, final String city, final String country)
			throws JSONException, IOException {
		JsonNode latLong = getLatLong(street, city, country);
		if (latLong == null) {
			return notFound(Constants.NOT_FOUND.concat(street).concat("+").concat(city).concat("+").concat(country));
		}
		return ok(latLong.get("longitude").asText());
	}

	private static JsonNode getPostalCode(final String aStreet, final String aCity, final String aCountry)
			throws JSONException, IOException {
		JsonNode geoNode = getFirstGeoNode(aStreet, aCity, aCountry);
		if (geoNode == null) {
			return null;
		}
		return geoNode.get(Constants.POSTALCODE);
	}

	public static JsonNode getLatLong(final String aQuery) throws JSONException, IOException {
		JsonNode geoNode = getFirstGeoNode(aQuery);
		if (geoNode == null) {
			return null;
		}
		return geoNode.get(Constants.GEOCODE);
	}

	public static JsonNode getLatLong(final String aStreet, final String aCity, final String aCountry)
			throws JSONException, IOException {
		JsonNode geoNode = getFirstGeoNode(aStreet, aCity, aCountry);
		if (geoNode == null) {
			return null;
		}
		return geoNode.get(Constants.GEOCODE);
	}

	private static JsonNode getFirstGeoNode(final String aStreet, final String aCity, final String aCountry)
			throws JSONException, IOException {
		SearchResponse response = LocalQuery.queryLocal(aStreet, aCity, aCountry);
		JsonNode geoNode;
		if (response == null || response.getHits().getTotalHits() == 0) {
			// this address information has never been queried before
			geoNode = NominatimQuery.createGeoNode(aStreet, aCity, aCountry);
			LocalQuery.addLocal(geoNode, GeoElasticsearch.ES_TYPE_NOMINATIM);
		} else {
			geoNode = MAPPER.valueToTree(response.getHits().hits()[0].getSource());
		}
		return geoNode;
	}

	private static JsonNode getFirstGeoNode(final String aQuery) throws JSONException, IOException {
		SearchResponse response = LocalQuery.queryLocal(aQuery);
		JsonNode geoNode;
		if (response == null || response.getHits().getTotalHits() == 0) {
			// this address information has never been queried before
			geoNode = WikidataQuery.createGeoNode(aQuery);
			LocalQuery.addLocal(geoNode, GeoElasticsearch.ES_TYPE_WIKIDATA);
		} else {
			geoNode = MAPPER.valueToTree(response.getHits().hits()[0].getSource());
		}
		return geoNode;
	}

}
