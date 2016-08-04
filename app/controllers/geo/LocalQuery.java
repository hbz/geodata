package controllers.geo;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Methods to query the local Elasticsearch index
 */
public class LocalQuery {

	/**
	 * Query the local Elasticsearch index
	 * 
	 * @param aTerm A search term
	 * @return The search result from the index
	 */
	public static SearchResponse queryLocal(final String aTerm) {
		SearchResponse response =
				GeoElasticsearch.ES_CLIENT.prepareSearch(GeoElasticsearch.ES_INDEX)
						.setQuery(QueryBuilders.termQuery(Constants.SEARCHTERM, aTerm))
						.execute().actionGet();
		return response;
	}

	/**
	 * Query the local Elasticsearch index with a given address
	 * 
	 * @param aStreet The treet name of the address
	 * @param aCity The city name of the address
	 * @param aCountry The country name of the address
	 * @return The search result from the index
	 */
	public static SearchResponse queryLocal(final String aStreet,
			final String aCity, final String aCountry) {
		final BoolQueryBuilder queryBuilder = boolQuery();
		queryBuilder.must(matchQuery(Constants.STREET, aStreet))
				.must(matchQuery(Constants.CITY, aCity));

		SearchRequestBuilder searchBuilder =
				GeoElasticsearch.ES_CLIENT.prepareSearch(GeoElasticsearch.ES_INDEX)
						.setTypes(GeoElasticsearch.ES_TYPE_NOMINATIM);
		return searchBuilder.setQuery(queryBuilder).setSize(1).execute()
				.actionGet();
	}

	/**
	 * Add a geo information to the Elasticsearch index
	 * 
	 * @param aGeoNode The geo node to be added
	 * @param aEsType The type of data of the geo node (nominatim_data vs.
	 *          wikidata_data)
	 */
	public static void addLocal(final JsonNode aGeoNode, String aEsType) {
		int retries = 40;
		while (retries > 0) {
			try {
				GeoElasticsearch.ES_CLIENT
						.prepareIndex(GeoElasticsearch.ES_INDEX, aEsType)
						.setSource(aGeoNode.toString()).execute().actionGet();
				GeoElasticsearch.ES_CLIENT.admin().indices()
						.refresh(new RefreshRequest()).actionGet();
				break; // stop retry-while
			} catch (NoNodeAvailableException e) {
				retries--;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException x) {
					x.printStackTrace();
				}
				System.err.printf("Retry indexing record %s: %s (%s more retries)\n",
						e.getMessage(), retries);
			}
		}
	}

}
