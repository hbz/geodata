package controllers.geo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

@SuppressWarnings("javadoc")
public class GeoElasticsearch {

	// ELASTICSEARCH SETTINGS
	protected static final String ES_CLUSTER = "organisation-cluster";
	protected static final String ES_INDEX = "organisation-geos";
	protected static final String ES_TYPE = "location";
	protected static final String SERVER_NAME = "localhost";
	protected static final String SETTINGS_FILE = "conf/geo-index-settings.json";

	// ELASTICSEARCH COMPONENTS
	protected static final Builder CLIENT_SETTINGS = ImmutableSettings.settingsBuilder().put("cluster.name", ES_CLUSTER)
			.put("index.name", ES_INDEX);
	private static InetSocketTransportAddress node = new InetSocketTransportAddress(SERVER_NAME, 9300);
	protected static TransportClient TC = new TransportClient(CLIENT_SETTINGS.put("client.transport.sniff", false)
			.put("client.transport.ping_timeout", 20, TimeUnit.SECONDS).build());
	protected static Client ES_CLIENT = TC.addTransportAddress(node);

	// for production
	public GeoElasticsearch() {
	}

	// for testing
	public GeoElasticsearch(Client aClient) {
		ES_CLIENT = aClient;
	}

	public static void createEmptyIndex(final Client aClient) throws IOException {
		deleteIndex(aClient);
		String settingsMappings = Files.lines(Paths.get(SETTINGS_FILE)).collect(Collectors.joining());
		CreateIndexRequestBuilder cirb = aClient.admin().indices().prepareCreate(ES_INDEX);
		cirb.setSource(settingsMappings);
		cirb.execute().actionGet();
		aClient.admin().indices().refresh(new RefreshRequest()).actionGet();
	}

	private static void deleteIndex(final Client aClient) {
		if (aClient.admin().indices().prepareExists(ES_INDEX).execute().actionGet().isExists()) {
			final DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(ES_INDEX);
			aClient.admin().indices().delete(deleteIndexRequest);
		}
	}
}
