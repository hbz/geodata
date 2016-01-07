package controllers.geo;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class WikidataQueryTest {

	@Test
	public void testGetFirstHit() throws JSONException, IOException {
		String search = "Nordrhein Westfalen";
		JSONObject result = WikidataQuery.getFirstHit(search);
		assertTrue(result.toString().contains("Land der Bundesrepublik Deutschland"));
	}

}
