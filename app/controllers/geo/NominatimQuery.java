package controllers.geo;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NominatimQuery {

	final private static ObjectMapper MAPPER = new ObjectMapper();
	final private static String mScheme = "http";
	final private static String mAuthority = "nominatim.openstreetmap.org";
	final private static String mPath = "/search.php";

	public static JSONObject getFirstHit(final String aStreetPlusNumber, final String aCity, final String aCountry)
			throws JSONException, IOException {
		String queryString = String
				.format("q=%s%s%s%s%s&addressdetails=1&format=json", aStreetPlusNumber, "%2C+", aCity, "%2C+", aCountry) //
				.replaceAll(" ", "%20");
		queryString = QueryHelpers.repairSpecialChars(queryString);
		String url = mScheme + "://" + mAuthority + mPath + "?" + queryString;
		JSONArray results = QueryHelpers.readJsonArrayFromUrl(url, 1000);
		if (results.length() == 0) {
			return null;
		}
		return results.getJSONObject(0);
	}

	public static JSONObject getFirstHit(final String aStreet, final String aNumber, final String aCity,
			final String aCountry) throws JSONException, IOException {
		return getFirstHit(aStreet + "+" + aNumber, aCity, aCountry);
	}

	public static double getLat(final JSONObject aGeoJson) {
		return aGeoJson.getDouble("lat");
	}

	public static double getLong(final JSONObject aGeoJson) {
		return aGeoJson.getDouble("lon");
	}

	public static Object getPostcode(final JSONObject aGeoJson) {
		return aGeoJson.getJSONObject("address").get("postcode");
	}

	public static ObjectNode createGeoNode(final String aStreet, final String aCity, final String aCountry)
			throws JSONException, IOException {
		// grid data of this geo node:
		ObjectNode geoNode = buildGeoNode(aStreet, aCity, aCountry);
		// data enrichment to this geo node:
		JSONObject nominatim = getFirstHit(aStreet, aCity, aCountry);
		if (nominatim != null) {
			double latitude = getLat(nominatim);
			double longitude = getLong(nominatim);
			String postalcode = (String) getPostcode(nominatim);
			geoNode.put(Constants.GEOCODE, new ObjectMapper().readTree( //
					String.format("{\"latitude\":\"%s\",\"longitude\":\"%s\"}", latitude, longitude)));
			geoNode.put(Constants.POSTALCODE, postalcode);
		}
		return geoNode;
	}

	private static ObjectNode buildGeoNode(final String aStreet, final String aCity, final String aCountry) {
		ObjectNode geoObject;
		geoObject = MAPPER.createObjectNode();
		geoObject.put(Constants.STREET, aStreet);
		geoObject.put(Constants.CITY, aCity);
		geoObject.put(Constants.COUNTRY, aCountry);
		return geoObject;
	}
}
