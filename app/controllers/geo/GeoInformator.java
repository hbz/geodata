package controllers.geo;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.mvc.Controller;
import play.mvc.Result;

public class GeoInformator extends Controller {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String STREET = "street";
	private static final String NUMBER = "number";
	private static final String CITY = "city";
	private static final String COUNTRY = "country";
	private static final String GEOCODE = "geocode";
	private static final String POSTALCODE = "postalcode";

	// private static final Client mClient = GeoElasticsearch.ES_CLIENT;

	// for production
	public GeoInformator() {
	}

	public static Result getPostCode(final String street, final String number, final String city, final String country)
			throws JSONException, IOException {
		return ok(getPostalCode(street, number, city, country).asText());
	}

	public static Result getLat(final String street, final String number, final String city, final String country)
			throws JSONException, IOException {
		return ok(getLatLong(street, number, city, country).get("latitude").asText());
	}

	public static Result getLong(final String street, final String number, final String city, final String country)
			throws JSONException, IOException {
		return ok(getLatLong(street, number, city, country).get("longitude").asText());
	}

	public static JsonNode getPostalCode(final String aStreet, final String aNumber, final String aCity,
			final String aCountry) throws JSONException, IOException {
		JsonNode geoNode = getFirstGeoNode(aStreet, aNumber, aCity, aCountry);
		return geoNode.get(POSTALCODE);
	}

	public static JsonNode getLatLong(final String aStreet, final String aNumber, final String aCity,
			final String aCountry) throws JSONException, IOException {
		JsonNode geoNode = getFirstGeoNode(aStreet, aNumber, aCity, aCountry);
		return geoNode.get(GEOCODE);
	}

	private static JsonNode getFirstGeoNode(final String aStreet, final String aNumber, final String aCity,
			final String aCountry) throws JSONException, IOException {
		SearchResponse response = queryLocal(aStreet, aNumber, aCity, aCountry);
		JsonNode geoNode;
		if (response == null || response.getHits().getTotalHits() == 0) {
			// this address information has never been queried before
			geoNode = createGeoNode(aStreet, aNumber, aCity, aCountry);
			addLocal(geoNode);
		} else {
			geoNode = MAPPER.valueToTree(response.getHits().hits()[0].getSource());
		}
		return geoNode;
	}

	private static SearchResponse queryLocal(final String aStreet, final String aNumber, final String aCity,
			final String aCountry) {
		final BoolQueryBuilder queryBuilder = boolQuery();
		queryBuilder.must(matchQuery(STREET, aStreet)).must(matchQuery(NUMBER, aNumber)).must(matchQuery(CITY, aCity));

		SearchRequestBuilder searchBuilder = GeoElasticsearch.ES_CLIENT.prepareSearch(GeoElasticsearch.ES_INDEX)
				.setTypes(GeoElasticsearch.ES_TYPE);
		return searchBuilder.setQuery(queryBuilder).setSize(1).execute().actionGet();
	}

	private static void addLocal(final JsonNode aGeoNode) {
		int retries = 40;
		while (retries > 0) {
			try {
				GeoElasticsearch.ES_CLIENT.prepareIndex(GeoElasticsearch.ES_INDEX, GeoElasticsearch.ES_TYPE)
						.setSource(aGeoNode.toString()).execute().actionGet();
				GeoElasticsearch.ES_CLIENT.admin().indices().refresh(new RefreshRequest()).actionGet();
				break; // stop retry-while
			} catch (NoNodeAvailableException e) {
				retries--;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException x) {
					x.printStackTrace();
				}
				System.err.printf("Retry indexing record %s: %s (%s more retries)\n", e.getMessage(), retries);
			}
		}
	}

	private static ObjectNode createGeoNode(final String aStreet, final String aNumber, final String aCity,
			final String aCountry) throws JSONException, IOException {
		ObjectNode geoObject;
		geoObject = MAPPER.createObjectNode();
		geoObject.put(STREET, aStreet);
		geoObject.put(NUMBER, aNumber);
		geoObject.put(CITY, aCity);
		geoObject.put(COUNTRY, aCountry);
		JSONObject nominatim = NominatimQuery.getFirstHit(aStreet, aNumber, aCity, aCountry);
		double latitude = getLat(nominatim);
		double longitude = getLong(nominatim);
		String postalcode = (String) getPostcode(nominatim);
		geoObject.put(GEOCODE, new ObjectMapper().readTree( //
				String.format("{\"latitude\":\"%s\",\"longitude\":\"%s\"}", latitude, longitude)));
		geoObject.put(POSTALCODE, postalcode);
		return geoObject;
	}

	private static double getLat(JSONObject aGeoJson) {
		return aGeoJson.getDouble("lat");
	}

	private static double getLong(JSONObject aGeoJson) {
		return aGeoJson.getDouble("lon");
	}

	private static Object getPostcode(JSONObject aGeoJson) {
		return aGeoJson.getJSONObject("address").get("postcode");
	}

}
