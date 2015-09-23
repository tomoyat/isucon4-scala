package models

import repositories.Users

import scala.util.{Try, Success, Failure}

object UserUtil {

    def attemptLogin(login: String, password: String, ip: String): Try[String] = {

        // get user by login
        val user = Users.getUserByLogin(login)

        // check ip banned

        // check user locked

        // check password
        val result = user match {
            case None => Failure(LoginException("Wrong username or password"))
            case Some(u) =>
                val saltPassword = password + ":" + u.salt
                val hashPassword = java.security.MessageDigest.getInstance("SHA-256").digest(saltPassword.getBytes("UTF-8"))
                val hashPasswordHex = hashPassword.map("%02x".format(_)).mkString
                u.password_hash match  {
                    case x if x == hashPasswordHex => Success(u.id.toString)
                    case _ => Failure(LoginException("Wrong username or password"))
                }
        }

        // update status
        result
    }

}
