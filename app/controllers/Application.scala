package controllers

import java.time.format.DateTimeFormatter

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import repositories.{LoginLogs, Users}

import scala.util.{Success, Failure, Try}

object Application extends Controller {

    implicit val reportWrites = new Writes[Report] {
        def writes(report: Report) = Json.obj(
            "banned_ips" -> report.bannedIps,
            "locked_users" -> report.lockedUsers
        )
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
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
            case Some(id) => Users.getUserById(id.toInt) match {
                case None => Redirect("/").flashing("notice" -> "You must be logged in")
                case Some(user) => {
                    val lastLogin = LoginLogs.getLastLoginById(user.id)
                    Ok(views.html.mypage(DisplayUser(lastLogin.login, lastLogin.ip,
                    lastLogin.created_at.format(formatter))))
                }
            }
        }
    }

    def report = Action { implicit request =>
        val ipList = LoginLogUtil.getBannedIPs
        val lockedNameList = LoginLogUtil.getLockedUserNames
        val report = Report(
            ipList,
            lockedNameList
        )
        val jsonResponse = Json.toJson(report)
        Ok(jsonResponse)
    }
}