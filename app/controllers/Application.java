package controllers;

import play.mvc.Controller;
import play.mvc.Result;

@SuppressWarnings("javadoc")
public class Application extends Controller {

	public static Result index() {
		return ok(views.html.index
				.render("lobid-geo-enrichment microservice is running"));
	}

}
