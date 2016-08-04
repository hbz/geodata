package controllers.geo;

import java.io.IOException;

import org.elasticsearch.action.search.SearchResponse;
import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Methods to trigger geo lookups on the index
 *
 */
@SuppressWarnings("javadoc")
public class GeoInformator extends Controller {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	// for production
	public GeoInformator() {
	}

	/**
	 * @param query Wiki data query
	 * @return The result of wiki data query
	 * @throws JSONException Thrown if first hit of json result cannot be returned
	 * @throws IOException Thrown if first hit of json result cannot be returned
	 */
	public static Result getWikiData(String query)
			throws JSONException, IOException {
		JsonNode geoNode = getFirstGeoNode(query);
		if (geoNode == null) {
			return notFound(Constants.NOT_FOUND.concat(query));
		}
		return ok(geoNode.toString());
	}

	/**
	 * Get the postal code for a given street address WITH house number
	 * 
	 * @param street The name of the street to find the postal code for
	 * @param number The house number to the find the postal code for
	 * @param city The name of the city to find the postal code for
	 * @param country The name of the country to find the postal code for
	 * @return The postal code for the given street address
	 * @throws JSONException Thrown if first hit of json result cannot be returned
	 * @throws IOException Thrown if first hit of json result cannot be returned
	 */
	public static Result getPostCodeExplicitNr(String street, String number,
			String city, String country) throws JSONException, IOException {
		return getPostCode(street + " " + number, city, country);
	}

	/**
	 * Get the lat coordinate for a given street address WITH house number
	 * 
	 * @param street The name of the street to find the coordinate for
	 * @param number The house number to the find the coordinate
	 * @param city The name of the city to find the coordinates for
	 * @param country The name of the country to find the coordinates for
	 * @return The lat coordinate for the given street address
	 * @throws JSONException Thrown if first hit of json result cannot be returned
	 * @throws IOException Thrown if first hit of json result cannot be returned
	 */
	public static Result getLatExplicitNr(String street, String number,
			String city, String country) throws JSONException, IOException {
		return getLat(street + " " + number, city, country);
	}

	/**
	 * Get the lon coordinate for a given street address WITH house number
	 * 
	 * @param street The name of the street to find the coordinate for
	 * @param number The house number to the find the coordinate for
	 * @param city The name of the city to find the coordinate for
	 * @param country The name of the country to find the coordinate for
	 * @return The lon coordinate for the given street address
	 * @throws JSONException Thrown if first hit of json result cannot be returned
	 * @throws IOException Thrown if first hit of json result cannot be returned
	 */
	public static Result getLongExplicitNr(String street, String number,
			String city, String country) throws JSONException, IOException {
		return getLong(street + " " + number, city, country);
	}

	/**
	 * Get the postal code for a given street address WITHOUT house number
	 * 
	 * @param street The name of the street to find the postal code for
	 * @param city The name of the city to find the postal code for
	 * @param country The name of the country to find the postal code for
	 * @return The postal code for the given street address
	 * @throws JSONException Thrown if first hit of json result cannot be returned
	 * @throws IOException Thrown if first hit of json result cannot be returned
	 */
	public static Result getPostCode(String street, String city, String country)
			throws JSONException, IOException {
		JsonNode postCode = getPostalCode(street, city, country);
		if (postCode == null) {
			return notFound(Constants.NOT_FOUND.concat(street).concat("+")
					.concat(city).concat("+").concat(country));
		}
		return ok(postCode.asText());
	}

	/**
	 * Get the lat coordinate for a given street address WITHOUT house number
	 * 
	 * @param street The name of the street to find the coordinate for
	 * @param city The name of the city to find the coordinate for
	 * @param country The name of the country to find the coordinate for
	 * @return The lat coordinate for the given street address
	 * @throws JSONException Thrown if first hit of json result cannot be returned
	 * @throws IOException Thrown if first hit of json result cannot be returned
	 */
	public static Result getLat(final String street, final String city,
			final String country) throws JSONException, IOException {
		JsonNode latLong = getLatLong(street, city, country);
		if (latLong == null) {
			return notFound(Constants.NOT_FOUND.concat(street).concat("+")
					.concat(city).concat("+").concat(country));
		}
		return ok(latLong.get("latitude").asText());
	}

	/**
	 * Get the lon coordinate for a given street address WITHOUT house number
	 * 
	 * @param street The name of the street to find the coordinate for
	 * @param city The name of the city to find the coordinate for
	 * @param country The name of the country to find the coordinate for
	 * @return The lon coordinate for the given street address
	 * @throws JSONException Thrown if first hit of json result cannot be returned
	 * @throws IOException Thrown if first hit of json result cannot be returned
	 */
	public static Result getLong(final String street, final String city,
			final String country) throws JSONException, IOException {
		JsonNode latLong = getLatLong(street, city, country);
		if (latLong == null) {
			return notFound(Constants.NOT_FOUND.concat(street).concat("+")
					.concat(city).concat("+").concat(country));
		}
		return ok(latLong.get("longitude").asText());
	}

	/**
	 * Get the lat coordinate for a given street address WITHOUT house number
	 * 
	 * @param street The name of the street to find the coordinates for
	 * @param city The name of the city to find the coordinates for
	 * @param country The name of the country to find the coordinates for
	 * @return The lat and lon coordinates for the given street address
	 * @throws JSONException Thrown if first hit of json result cannot be returned
	 * @throws IOException Thrown if first hit of json result cannot be returned
	 */
	public static JsonNode getLatLong(final String street, final String city,
			final String country) throws JSONException, IOException {
		JsonNode geoNode = getFirstGeoNode(street, city, country);
		if (geoNode == null) {
			return null;
		}
		return geoNode.get(Constants.GEOCODE);
	}

	private static JsonNode getPostalCode(final String aStreet,
			final String aCity, final String aCountry)
			throws JSONException, IOException {
		JsonNode geoNode = getFirstGeoNode(aStreet, aCity, aCountry);
		if (geoNode == null) {
			return null;
		}
		return geoNode.get(Constants.POSTALCODE);
	}

	private static JsonNode getFirstGeoNode(final String aStreet,
			final String aCity, final String aCountry)
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

	private static JsonNode getFirstGeoNode(final String aQuery)
			throws JSONException, IOException {
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
