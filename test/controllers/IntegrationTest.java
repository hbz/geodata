package controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.HTMLUNIT;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.junit.Test;

import play.libs.F.Callback;
import play.test.TestBrowser;

public class IntegrationTest {

	/**
	 * This integration test uses Solenium to test the app with a browser
	 */
	@Test
	public void test() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
			public void invoke(TestBrowser browser) {
				browser.goTo("http://localhost:3333/geodata/search");
				assertThat(browser.pageSource()).contains("lobid-geo-enrichment microservice is running");
			}
		});
	}

}
