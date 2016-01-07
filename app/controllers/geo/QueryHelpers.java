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

public class QueryHelpers {

	final private static String[] mBadSpecialChars = { "Ã", "Ã", "Ã", "Ã¤", "Ã¶", "Ã¼", "Ã" };
	final private static String[] mGoodSpecialChars = { "Ä", "Ö", "Ü", "ä", "ö", "ü", "ß" };

	static {
		assert(mBadSpecialChars.length == mGoodSpecialChars.length);
	}

	public static JSONObject readJsonObjectFromUrl(String aUrl, int aSleepMs) throws IOException, JSONException {
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
			Thread.sleep(aSleepMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
		return new JSONObject(sb.toString());
	}

	public static JSONArray readJsonArrayFromUrl(String aUrl, int aSleepMs) throws IOException, JSONException {
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
			Thread.sleep(aSleepMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
		return new JSONArray(sb.toString());
	}

	public static String repairSpecialChars(String aQuery) {
		return StringUtils.replaceEach(aQuery, mBadSpecialChars, mGoodSpecialChars);
	}
}
