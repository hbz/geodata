package controllers.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NominatimQuery {

	static {
		System.setProperty("http.agent", "java.net.URLConnection, email=<semweb@hbz-nrw.de>");
	}

	public static JSONObject getFirstHit(final String aStreetPlusNumber, final String aCity, final String aCountry)
			throws JSONException, IOException {
		String queryString = String
				.format("http://nominatim.openstreetmap.org/search.php?q=%s%s%s%s%s&addressdetails=1&format=json", //
						aStreetPlusNumber, "%2C+", aCity, "%2C+", aCountry)
				.replaceAll(" ", "%20");
		JSONArray results = readJsonArrayFromUrl(queryString);
		if (results.length() == 0) {
			return null;
		}
		return results.getJSONObject(0);
	}

	public static JSONObject getFirstHit(final String aStreet, final String aNumber, final String aCity,
			final String aCountry) throws JSONException, IOException {
		return getFirstHit(aStreet + "+" + aNumber, aCity, aCountry);
	}

	private static JSONArray readJsonArrayFromUrl(String aUrl) throws IOException, JSONException {
		URLConnection connection = new URL(aUrl).openConnection();
		connection.setRequestProperty("http.agent", GeoElasticsearch.HTTP_AGENT);
		InputStream is = connection.getInputStream();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
			int value;
			while ((value = reader.read()) != -1) {
				sb.append((char) value);
			}
			return new JSONArray(sb.toString());
		} finally {
			is.close();
		}
	}
}