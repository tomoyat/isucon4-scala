package controllers

import models.{LoginException, LoginForm}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import repositories.Users

import scala.util.{Failure, Try}

object Application extends Controller {

    val loginForm: Form[LoginForm] = Form(
        mapping("login" -> text, "password" -> text)(LoginForm.apply)(LoginForm.unapply)
    )

    def attemptLogin(request: Request[AnyContent], loginData: LoginForm): Try[String] = {
        val ip = request.headers.get("x-forwarded-for").getOrElse(request.remoteAddress)
        val login = loginData.login
        val password = loginData.password
        Failure(LoginException("This account is locked."))
    }

    def index = Action { implicit request =>
        val notice: String = request.flash.get("notice").getOrElse("")
        Ok(views.html.isucon("", notice))
    }

    def login = Action { implicit request =>
        loginForm.bindFromRequest.fold(
            hasErrors => {
                Redirect("/").flashing("notice" -> "Wrong username or password")
            },
            loginData => {
                val result: Result = attemptLogin(request, loginData) match {
                    case Failure(LoginException(msg)) => Redirect("/").flashing("notice" -> msg)
                    case _ => Redirect("/")
                }
                result
            }
        )
    }

    def dbtest = Action { implicit request =>
        Users.getUser
        Ok(views.html.dbtest(""))
    }
}