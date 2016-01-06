package controllers.geo;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WikidataQuery {

	final private static String SCHEME = "https";
	final private static String AUTHORITY = "www.wikidata.org";
	final private static String PATH_STEP_1 = "/w/api.php?action=query&list=search&format=json&srsearch=";
	final private static String PATH_STEP_2 = "/wiki/Special:EntityData/";
	final private static String SUFFIX_STEP_2 = ".json";

	public static JSONObject getFirstHit(final String aSearchKey) throws JSONException, IOException {
		String queryString = aSearchKey.replaceAll(" ", "%20");
		queryString = QueryHelpers.repairSpecialChars(queryString);
		JSONArray hitsStep1 = wikidataQueryStep1(queryString, 500);
		if (hitsStep1.length() == 0) {
			return null;
		}
		JSONObject firstHit = hitsStep1.getJSONObject(0);
		String id = firstHit.getString("title");
		JSONObject record = wikidataQueryStep2(id, 500);
		return record;
	}

	private static JSONArray wikidataQueryStep1(final String aQueryString, final int aSleepMs)
			throws JSONException, IOException {
		String url = SCHEME + "://" + AUTHORITY + PATH_STEP_1 + "?" + aQueryString;
		JSONObject result = QueryHelpers.readJsonObjectFromUrl(url, aSleepMs);
		JSONArray hits = result.getJSONObject("query").getJSONArray("search");
		return hits;
	}

	private static JSONObject wikidataQueryStep2(final String aQueryId, final int aSleepMs)
			throws JSONException, IOException {
		String url = SCHEME + "://" + AUTHORITY + PATH_STEP_2 + aQueryId + SUFFIX_STEP_2;
		JSONObject result = QueryHelpers.readJsonObjectFromUrl(url, aSleepMs);
		return result;
	}

	public static double getLat(JSONObject aGeoJson) {
		return aGeoJson.getJSONObject("entities").getJSONObject("Q365").getJSONObject("claims").getJSONArray("P625")
				.getJSONObject(0).getJSONObject("mainsnak").getJSONObject("datavalue").getJSONObject("value")
				.getDouble("latitude");
	}

	public static double getLong(JSONObject aGeoJson) {
		return aGeoJson.getJSONObject("entities").getJSONObject("Q365").getJSONObject("claims").getJSONArray("P625")
				.getJSONObject(0).getJSONObject("mainsnak").getJSONObject("datavalue").getJSONObject("value")
				.getDouble("longitude");
	}
}
