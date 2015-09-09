import static org.junit.Assert.assertEquals;
import static play.test.Helpers.contentAsString;

import java.io.IOException;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import controllers.geo.GeoInformator;

@SuppressWarnings("javadoc")
public class GeoInformatorTest {

	// private static Node node;
	// private static Client client;
	// private static GeoInformator geoInformator;

	@BeforeClass
	public static void makeIndex() throws IOException, InterruptedException {
		// node = nodeBuilder().local(true).node();
		// client = node.client();
		// GeoElasticsearch geoES = new GeoElasticsearch(client);
		// geoES.createEmptyIndex(client);
		// GeoElasticsearch.createEmptyIndex(client);
		// geoInformator = new GeoInformator();
		// Thread.sleep(1000);
	}

	@Test
	public void testPostcode() throws JSONException, IOException {
		String street = "Jülicher Straße";
		String number = "6";
		String city = "Köln";
		String country = "Germany";
		String postalCode = contentAsString(GeoInformator.getPostCode(street, number, city, country));
		assertEquals("50674", postalCode);
	}

	@Test
	public void testLatLong() throws JSONException, IOException {
		String street = "Jülicher Straße";
		String number = "6";
		String city = "Köln";
		String country = "Germany";
		String latLong = GeoInformator.getLatLong(street, number, city, country).toString();
		assertEquals("{\"latitude\":\"50.9341361\",\"longitude\":\"6.93551400842729\"}", latLong);
	}

	@Test
	public void testLat() throws JSONException, IOException {
		String street = "Jülicher Straße";
		String number = "6";
		String city = "Köln";
		String country = "Germany";
		String latitude = contentAsString(GeoInformator.getLat(street, number, city, country));
		assertEquals("50.9341361", latitude);
	}

	@Test
	public void testLong() throws JSONException, IOException {
		String street = "Jülicher Straße";
		String number = "6";
		String city = "Köln";
		String country = "Germany";
		String longitude = contentAsString(GeoInformator.getLong(street, number, city, country));
		assertEquals("6.93551400842729", longitude);
	}

	@AfterClass
	public static void closeElasticSearch() {
		// client.close();
		// node.close();
	}

}
