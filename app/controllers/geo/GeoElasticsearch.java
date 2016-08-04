package controllers.geo;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Configuration and creation of Elasticsearch index
 */
@SuppressWarnings("javadoc")
public class GeoElasticsearch {

	private static final Config CONFIG =
			ConfigFactory.parseFile(new File("conf/application.conf")).resolve();

	// COMMON SETTINGS

	protected static final String HTTP_AGENT =
			"java.net.URLConnection, email=<semweb@hbz-nrw.de>";

	// ELASTICSEARCH SETTINGS
	protected static final String ES_CLUSTER = "elasticsearch";
	protected static final String ES_INDEX = "testindex";
	protected static final String ES_TYPE_NOMINATIM = "nominatim_data";
	protected static final String ES_TYPE_WIKIDATA = "wikidata_data";
	protected static final String SERVER_NAME = "localhost";
	protected static final String SETTINGS_FILE = "conf/geo-index-settings.json";

	// ELASTICSEARCH COMPONENTS
	protected static final Settings CLIENT_SETTINGS = Settings.settingsBuilder()
			.put("cluster.name", ES_CLUSTER).put("index.name", ES_INDEX)
			.put("client.transport.sniff", false)
			.put("client.transport.ping_timeout", 20, TimeUnit.SECONDS)
			.put("path.home", ".")
			.put("http.port", CONFIG.getString("index.es.port.http"))
			.put("transport.tcp.port", CONFIG.getString("index.es.port.tcp")).build();

	private static Node node =
			nodeBuilder().settings(CLIENT_SETTINGS).local(true).node();
	public static Client ES_CLIENT = node.client();

	// for production
	public GeoElasticsearch() {
	}

	// for testing
	public GeoElasticsearch(Client aClient) {
		ES_CLIENT = aClient;
	}

	/**
	 * Create an new Elasticsearch index
	 * 
	 * @param aClient An Elasticsearch client
	 * @throws IOException Thrown if settings file cannot be read
	 */
	public static void createIndex(final Client aClient) throws IOException {
		String settingsMappings =
				Files.lines(Paths.get(SETTINGS_FILE)).collect(Collectors.joining());
		CreateIndexRequestBuilder cirb =
				aClient.admin().indices().prepareCreate(ES_INDEX);
		cirb.setSource(settingsMappings);
		cirb.execute().actionGet();
	}

	public static void refreshIndex(final Client aClient) {
		aClient.admin().indices().refresh(new RefreshRequest()).actionGet();
	}

	/**
	 * Check whether the Elasticsearch index already exists
	 * 
	 * @param aClient An Elasticsearch index
	 * @return Whether index already exists or not
	 */
	public static boolean hasIndex(final Client aClient) {
		aClient.admin().cluster().prepareHealth().setWaitForYellowStatus().execute()
				.actionGet();
		return aClient.admin().indices().prepareExists(ES_INDEX).execute()
				.actionGet().isExists();
	}

	public static Client getClient() {
		return ES_CLIENT;
	}
}
