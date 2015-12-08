package controllers;

import java.io.IOException;

import controllers.geo.GeoElasticsearch;
import play.Application;
import play.GlobalSettings;
import play.Logger;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		Logger.info("geodata start...");
		try {
			if (!GeoElasticsearch.hasIndex(GeoElasticsearch.getClient())) {
				GeoElasticsearch.createIndex(GeoElasticsearch.getClient());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		GeoElasticsearch.refreshIndex(GeoElasticsearch.getClient());
	}

	@Override
	public void onStop(Application app) {
		Logger.info("geodata shutdown...");
	}

}
