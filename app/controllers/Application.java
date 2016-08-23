package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Application Controller to render index
 */
public class Application extends Controller {

	/**
	 * Render index page
	 * 
	 * @return Play mvc result object
	 */
	public static Result index() {
		return ok(views.html.index
				.render("lobid-geo-enrichment microservice is running"));
	}

}
