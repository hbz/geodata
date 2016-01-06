package controllers.geo;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NominatimQuery {

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

}
