package controllers.geo;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Methods to query the nominatim api
 */
public class NominatimQuery {

	final private static ObjectMapper MAPPER = new ObjectMapper();
	final private static String mScheme = "http";
	final private static String mAuthority = "nominatim.openstreetmap.org";
	final private static String mPath = "/search.php";

	private static JSONObject getFirstHit(final String aStreetPlusNumber,
			final String aCity, final String aCountry) {
		String queryString = String
				.format("q=%s%s%s%s%s&addressdetails=1&format=json", aStreetPlusNumber,
						"%2C+", aCity, "%2C+", aCountry) //
				.replaceAll(" ", "%20");
		queryString = QueryHelpers.repairSpecialChars(queryString);
		String url = mScheme + "://" + mAuthority + mPath + "?" + queryString;
		JSONArray results = QueryHelpers.readJsonArrayFromUrl(url, 1000);
		if (results == null || results.length() == 0) {
			return null;
		}
		return results.getJSONObject(0);
	}

	/**
	 * Get the first hit from the search result on nominatim
	 * 
	 * @param aStreet The street of the address
	 * @param aNumber The house number of the address
	 * @param aCity The city name of the address
	 * @param aCountry The country name of the address
	 * @return the first hit from the search result on nominatim
	 * @throws JSONException Thrown if Json cannot be read from URL or first Json
	 *           Object cannot be returned from result set
	 * @throws IOException Thrown if Json cannot be read from URL
	 */
	public static JSONObject getFirstHit(final String aStreet,
			final String aNumber, final String aCity, final String aCountry)
					throws JSONException, IOException {
		return getFirstHit(aStreet + "+" + aNumber, aCity, aCountry);
	}

	private static double getLat(final JSONObject aGeoJson) {
		return aGeoJson.getDouble("lat");
	}

	private static double getLong(final JSONObject aGeoJson) {
		return aGeoJson.getDouble("lon");
	}

	private static Object getPostcode(final JSONObject aGeoJson) {
		JSONObject address = aGeoJson.getJSONObject("address");
		return address.has("postcode") ? address.get("postcode") : null;
	}

	/**
	 * Create a new Nominatim Geo Node to be added to the local Elasticsearch
	 * index
	 * 
	 * @param aStreet The street of the address to be packed in the geo node
	 * @param aCity The city name of the address to be packed in the geo node
	 * @param aCountry The country name of the address to be packed in the geo
	 *          node
	 * @return The Geo Node for geo information
	 * @throws IllegalStateException If we could not create a JSON object
	 */
	public static ObjectNode createGeoNode(final String aStreet,
			final String aCity, final String aCountry) throws IllegalStateException {
		// grid data of this geo node:
		ObjectNode geoNode = buildGeoNode(aStreet, aCity, aCountry);
		// data enrichment to this geo node:
		JSONObject nominatim = getFirstHit(aStreet, aCity, aCountry);
		if (nominatim != null) {
			double latitude = getLat(nominatim);
			double longitude = getLong(nominatim);
			String postalcode = (String) getPostcode(nominatim);
			JsonNode node = null;
			try {
				node = new ObjectMapper().readTree( //
						String.format("{\"latitude\":\"%s\",\"longitude\":\"%s\"}",
								latitude, longitude));
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalStateException("Could not create JSON object", e);
			}
			if (node != null) {
				geoNode.put(Constants.GEOCODE, node);
			}
			if (postalcode != null) {
				geoNode.put(Constants.POSTALCODE, postalcode);
			}
		}
		return geoNode;
	}

	private static ObjectNode buildGeoNode(final String aStreet,
			final String aCity, final String aCountry) {
		ObjectNode geoObject;
		geoObject = MAPPER.createObjectNode();
		geoObject.put(Constants.STREET, aStreet);
		geoObject.put(Constants.CITY, aCity);
		geoObject.put(Constants.COUNTRY, aCountry);
		return geoObject;
	}
}
