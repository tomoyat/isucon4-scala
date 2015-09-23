package controllers

import models.{UserUtil, LoginException, LoginForm}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import repositories.Users

import scala.util.{Success, Failure, Try}

object Application extends Controller {

    val loginForm: Form[LoginForm] = Form(
        mapping("login" -> text, "password" -> text)(LoginForm.apply)(LoginForm.unapply)
    )

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
                val ip = request.headers.get("x-forwarded-for").getOrElse(request.remoteAddress)
                val login = loginData.login
                val password = loginData.password
                val result: Result = UserUtil.attemptLogin(login, password, ip) match {
                    case Failure(LoginException(msg)) => Redirect("/").flashing("notice" -> msg)
                    case Success(id) => Redirect("/mypage").withSession(request.session + ("userId" -> id))
                }
                result
            }
        )
    }

    def mypage = Action { implicit request =>
        request.session.get("userId") match {
            case None => Redirect("/").flashing("notice" -> "You must be logged in")
            case _ => Ok(views.html.mypage())
        }
    }
}