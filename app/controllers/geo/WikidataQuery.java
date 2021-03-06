package controllers.geo;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WikidataQuery {

	final private static ObjectMapper MAPPER = new ObjectMapper();
	final private static String SCHEME = "https";
	final private static String AUTHORITY = "www.wikidata.org";
	final private static String PATH_STEP_1 =
			"/w/api.php?action=query&list=search&format=json&srsearch=";
	final private static String PATH_STEP_2 = "/wiki/Special:EntityData/";
	final private static String SUFFIX_STEP_2 = ".json";

	/**
	 * Get the first hit of the wikidata search results
	 * 
	 * @param aSearchKey The wikidata search query
	 * @return The first hit of the wikidata search results
	 * @throws JSONException Thrown if wikidata search hits cannot be extracted
	 *           from results or first Json Object cannot be returned from hits
	 * @throws IOException Thrown if Json cannot be read from URL
	 */
	public static JSONObject getFirstHit(final String aSearchKey)
			throws JSONException, IOException {
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

	private static JSONArray wikidataQueryStep1(final String aQueryString,
			final int aSleepMs) throws JSONException, IOException {
		String url = SCHEME + "://" + AUTHORITY + PATH_STEP_1 + "?" + aQueryString;
		JSONObject result = QueryHelpers.readJsonObjectFromUrl(url, aSleepMs);
		JSONArray hits = result.getJSONObject("query").getJSONArray("search");
		return hits;
	}

	private static JSONObject wikidataQueryStep2(final String aQueryId,
			final int aSleepMs) throws JSONException, IOException {
		String url =
				SCHEME + "://" + AUTHORITY + PATH_STEP_2 + aQueryId + SUFFIX_STEP_2;
		JSONObject result = QueryHelpers.readJsonObjectFromUrl(url, aSleepMs);
		return result;
	}

	private static double getLat(final JSONObject aGeoJson, final String aId) {
		return aGeoJson.getJSONObject("entities").getJSONObject(aId)
				.getJSONObject("claims").getJSONArray("P625").getJSONObject(0)
				.getJSONObject("mainsnak").getJSONObject("datavalue")
				.getJSONObject("value").getDouble("latitude");
	}

	private static double getLong(final JSONObject aGeoJson, final String aId) {
		return aGeoJson.getJSONObject("entities").getJSONObject(aId)
				.getJSONObject("claims").getJSONArray("P625").getJSONObject(0)
				.getJSONObject("mainsnak").getJSONObject("datavalue")
				.getJSONObject("value").getDouble("longitude");
	}

	private static String getLabel(final JSONObject aGeoJson, final String aId) {
		final JSONObject labels = aGeoJson.getJSONObject("entities")
				.getJSONObject(aId).getJSONObject("labels");
		if (labels.getJSONObject("de") != null) {
			return labels.getJSONObject("de").getString("value");
		}
		return null;
	}

	/**
	 * Create a new Nominatim Geo Node to be added to the local Elasticsearch
	 * index
	 * 
	 * @param aQuery The query that is used to get the information from wikidata
	 * @return The Geo Node for geo information
	 * @throws JSONException Thrown if Json cannot be read from URL or first Json
	 *           Object cannot be returned from result set
	 * @throws IOException Thrown if Json cannot be read from URL
	 */
	public static ObjectNode createGeoNode(final String aQuery)
			throws JSONException, IOException {

		// grid data of this geo node:
		ObjectNode geoNode = buildGeoNode(aQuery);

		// data enrichment to this geo node:
		JSONObject wikidata = getFirstHit(aQuery);
		if (wikidata != null) {
			String id = getId(wikidata);
			geoNode.put(Constants.ID, id);

			String label = getLabel(wikidata, id);
			geoNode.put(Constants.LABEL, label);

			double latitude = getLat(wikidata, id);
			double longitude = getLong(wikidata, id);
			geoNode.put(Constants.GEOCODE,
					new ObjectMapper().readTree( //
							String.format("{\"latitude\":\"%s\",\"longitude\":\"%s\"}",
									latitude, longitude)));
		}
		return geoNode;
	}

	private static ObjectNode buildGeoNode(final String aQuery) {
		ObjectNode geoObject;
		geoObject = MAPPER.createObjectNode();
		geoObject.put(Constants.SEARCHTERM, aQuery);
		return geoObject;
	}

	private static String getId(final JSONObject aWikidataResult) {
		if (aWikidataResult != null) {
			Iterator<String> keys = aWikidataResult.getJSONObject("entities").keys();
			while (keys.hasNext()) {
				String key = keys.next();
				if (isWikidataKey(key)) {
					return key;
				}
			}
		}
		return null;
	}

	private static boolean isWikidataKey(String aKey) {
		if (StringUtils.isEmpty(aKey))
			return false;
		if (aKey.length() < 2)
			return false;
		if (!aKey.startsWith("Q"))
			return false;
		if (!aKey.substring(1).matches("[0-9]*"))
			return false;
		return true;
	}

}
