package controllers

import play.api.mvc._

object Application extends Controller {

    def index = Action { implicit request =>
        val notice: String = request.flash.get("notice").getOrElse("")
        Ok(views.html.isucon("", notice))
    }
}