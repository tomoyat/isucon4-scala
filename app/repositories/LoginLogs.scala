package repositories

import java.time.{ZoneId, ZonedDateTime}

import models.{LoginLog, User}
import scalikejdbc.{AutoSession, WrappedResultSet, SQL}

import scala.util.{Success, Failure, Try}

object LoginLogs {

    implicit val session = AutoSession

    val zoneId = ZoneId.of("JST", ZoneId.SHORT_IDS)

    val loginLogMapping = (rs: WrappedResultSet) => LoginLog(
        id = rs.long("id"),
        created_at = ZonedDateTime.of(rs.timestamp("created_at").toLocalDateTime, zoneId),
        user_id = rs.intOpt("user_id"),
        login = rs.string("login"),
        ip = rs.string("ip"),
        succeeded = rs.int("succeeded")
    )

    def update(user: Option[User], login: String, ip: String, succeeded: Try[String]) {
        val insertSQL = SQL("INSERT INTO login_log (`created_at`, `user_id`, `login`, `ip`, `succeeded`) VALUES (?,?,?,?,?)")
        val createdAt = ZonedDateTime.now(zoneId)

        val userId: Option[Int] = user match {
            case None => None
            case Some(u) => Some(u.id)
        }
        val succeededVal: Int = succeeded match {
            case Success(x) => 1
            case _ => 0
        }
        insertSQL.bind(createdAt, userId, login, ip, succeededVal).update.apply()
    }

    def getLastLoginById(id: Int): LoginLog = {
        val sql = SQL("SELECT * FROM login_log WHERE succeeded = 1 AND user_id = ? ORDER BY id DESC LIMIT 2")
        val logs: List[LoginLog] = sql.bind(id).map(loginLogMapping).list.apply()

        logs.last
    }
}
