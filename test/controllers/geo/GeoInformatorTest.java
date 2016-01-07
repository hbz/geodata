package controllers.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.contentAsString;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class GeoInformatorTest {

	@Test
	public void testPostcode() throws JSONException, IOException {
		String street = "Jülicher Straße 6";
		String city = "Köln";
		String country = "Germany";
		String postalCode = contentAsString(GeoInformator.getPostCode(street, city, country));
		assertEquals("50674", postalCode);
	}

	@Test
	public void testLatLong() throws JSONException, IOException {
		String street = "Jülicher Straße 6";
		String city = "Köln";
		String country = "Germany";
		String latLong = GeoInformator.getLatLong(street, city, country).toString();
		assertEquals("{\"latitude\":\"50.9341361\",\"longitude\":\"6.93551400842729\"}", latLong);
	}

	@Test
	public void testLat() throws JSONException, IOException {
		String street = "Jülicher Straße 6";
		String city = "Köln";
		String country = "Germany";
		String latitude = contentAsString(GeoInformator.getLat(street, city, country));
		assertEquals("50.9341361", latitude);
	}

	@Test
	public void testLong() throws JSONException, IOException {
		String street = "Jülicher Straße 6";
		String city = "Köln";
		String country = "Germany";
		String longitude = contentAsString(GeoInformator.getLong(street, city, country));
		assertEquals("6.93551400842729", longitude);
	}

	@Test
	public void test4Parameters() throws JSONException, IOException {
		String street = "Jülicher Straße";
		String number = "6";
		String city = "Köln";
		String country = "Germany";
		String longitude = contentAsString(GeoInformator.getLongExplicitNr(street, number, city, country));
		assertEquals("6.93551400842729", longitude);
	}

	@Test
	public void testNonExistingAddressLat() throws JSONException, IOException {
		String street = "All people are equal";
		String number = "123456789";
		String city = "Justice";
		String country = "Land of Peace And Hope";
		assertTrue(contentAsString(GeoInformator.getLatExplicitNr(street, number, city, country)).contains("404"));
	}

	@Test
	public void testNonExistingAddressLong() throws JSONException, IOException {
		String street = "All people are equal";
		String number = "123456789";
		String city = "Justice";
		String country = "Land of Peace And Hope";
		assertTrue(contentAsString(GeoInformator.getLongExplicitNr(street, number, city, country)).contains("404"));
	}

	@Test
	public void testNonExistingAddressPostcode() throws JSONException, IOException {
		String street = "All people are equal";
		String number = "123456789";
		String city = "Justice";
		String country = "Land of Peace And Hope";
		assertTrue(contentAsString(GeoInformator.getPostCodeExplicitNr(street, number, city, country)).contains("404"));
	}

	@Test
	public void testWikidata() throws JSONException, IOException {
		String query = "Köln";
		String latAndlong = contentAsString(GeoInformator.getLatAndLong(query));
		assertTrue(latAndlong.contains("50.942"));
		assertTrue(latAndlong.contains("6.95777"));
	}

}
