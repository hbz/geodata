package controllers.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NominatimQuery {

	final private static String mScheme = "http";
	final private static String mAuthority = "nominatim.openstreetmap.org";
	final private static String mPath = "/search.php";
	final private static String[] mBadSpecialChars = { "Ã", "Ã", "Ã", "Ã¤", "Ã¶", "Ã¼", "Ã" };
	final private static String[] mGoodSpecialChars = { "Ä", "Ö", "Ü", "ä", "ö", "ü", "ß" };

	static {
		assert(mBadSpecialChars.length == mGoodSpecialChars.length);
	}

	public static JSONObject getFirstHit(final String aStreetPlusNumber, final String aCity, final String aCountry)
			throws JSONException, IOException {
		String queryString = String
				.format("q=%s%s%s%s%s&addressdetails=1&format=json", aStreetPlusNumber, "%2C+", aCity, "%2C+", aCountry) //
				.replaceAll(" ", "%20");
		queryString = repairSpecialChars(queryString);
		String url = mScheme + "://" + mAuthority + mPath + "?" + queryString;
		JSONArray results = readJsonArrayFromUrl(url);
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
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

			int value;
			while ((value = reader.read()) != -1) {
				sb.append((char) value);
			}
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
		return new JSONArray(sb.toString());
	}

	private static String repairSpecialChars(String aQuery) {
		return StringUtils.replaceEach(aQuery, mBadSpecialChars, mGoodSpecialChars);
	}
}
