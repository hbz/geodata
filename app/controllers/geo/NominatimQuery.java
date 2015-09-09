package controllers.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NominatimQuery {

	public static JSONObject getFirstHit(final String aStreet, final String aNumber, final String aCity,
			final String aCountry) throws JSONException, IOException {
		String queryString = String
				.format("http://nominatim.openstreetmap.org/search.php?q=%s+%s%s%s%s%s&addressdetails=1&format=json", //
						aStreet, aNumber, "%2C+", aCity, "%2C+", aCountry)
				.replaceAll(" ", "%20");
		return readJsonArrayFromUrl(queryString).getJSONObject(0);
	}

	private static JSONArray readJsonArrayFromUrl(String aUrl) throws IOException, JSONException {
		InputStream is = new URL(aUrl).openStream();
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