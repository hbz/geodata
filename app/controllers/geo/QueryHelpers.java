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

/**
 * Helper methods for queries
 */
public class QueryHelpers {

	final private static String[] mBadSpecialChars =
			{ "Ã", "Ã", "Ã", "Ã¤", "Ã¶", "Ã¼", "Ã" };
	final private static String[] mGoodSpecialChars =
			{ "Ä", "Ö", "Ü", "ä", "ö", "ü", "ß" };

	static {
		assert(mBadSpecialChars.length == mGoodSpecialChars.length);
	}

	/**
	 * Method to read a JSON object from a URL
	 * 
	 * @param aUrl The url to read the JSON object from
	 * @param aSleepMs The number of miliseconds for the thread to sleep after
	 *          reading the input stream
	 * @return The JSON string read from the URL as a JSON objects
	 * @throws IOException Thrown if the connection cannot be opened, the input
	 *           stream cannot be received from the connection or the reader
	 *           cannot read the input stream
	 * @throws JSONException Thrown if the string from the input stream cannot be
	 *           converted to an JSON object
	 */
	public static JSONObject readJsonObjectFromUrl(String aUrl, int aSleepMs)
			throws IOException, JSONException {
		URLConnection connection = new URL(aUrl).openConnection();
		connection.setRequestProperty("http.agent", GeoElasticsearch.HTTP_AGENT);
		try (InputStream is = connection.getInputStream()) {
			StringBuilder sb = new StringBuilder();
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, Charset.forName("UTF-8")));
				int value;
				while ((value = reader.read()) != -1) {
					sb.append((char) value);
				}
				Thread.sleep(aSleepMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return new JSONObject(sb.toString());
		}
	}

	/**
	 * Method to read a JSON array from a URL
	 * 
	 * @param aUrl The url to read the JSON array from
	 * @param aSleepMs aSleepMs The number of miliseconds for the thread to sleep
	 *          after reading the input stream
	 * @return The string from the input stream as a JSON array object, or null
	 */
	public static JSONArray readJsonArrayFromUrl(String aUrl, int aSleepMs) {
		try {
			URLConnection connection = new URL(aUrl).openConnection();
			connection.setRequestProperty("http.agent", GeoElasticsearch.HTTP_AGENT);
			try (InputStream is = connection.getInputStream()) {
				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is, Charset.forName("UTF-8")));
					int value;
					while ((value = reader.read()) != -1) {
						sb.append((char) value);
					}
					Thread.sleep(aSleepMs);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return new JSONArray(sb.toString());
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Method to replace special characters (like Umlaute) in a query string
	 * 
	 * @param aQuery The query string containing the special characters
	 * @return The query string with the fixed characters
	 */
	public static String repairSpecialChars(String aQuery) {
		return StringUtils.replaceEach(aQuery, mBadSpecialChars, mGoodSpecialChars);
	}
}
