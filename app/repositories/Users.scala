package repositories

import models.User
import scalikejdbc._
object Users {
    implicit val session = AutoSession

    val userMapping = (rs: WrappedResultSet) => User(
        id = rs.long("id"),
        login = rs.string("login"),
        password_hash = rs.string("password_hash"),
        salt = rs.string("salt")
    )

    def getUserByLogin(login: String): Option[User] = {
        val result: List[User] = SQL("select * from users where login = ?").bind(login).map(userMapping).list.apply()

        if (result.isEmpty) {
            return None
        }
        Some(result.head)
    }
}
