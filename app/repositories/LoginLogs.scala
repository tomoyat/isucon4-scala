package repositories

import java.time.{ZoneId, ZonedDateTime}

import models._
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

    val lastLoginIPMapping = (rs: WrappedResultSet) => LastLoginIP(
        ip = rs.string("ip"),
        lastLoginId = rs.long("last_login_id")
    )

    val lastLoginNameMapping = (rs: WrappedResultSet) => LastLoginName(
        userId = rs.int("user_id"),
        login = rs.string("login"),
        lastLoginId = rs.long("last_login_id")
    )


    def update(user: Option[User], login: String, ip: String, succeeded: Int): Unit = {
        val insertSQL = SQL("INSERT INTO login_log (`created_at`, `user_id`, `login`, `ip`, `succeeded`) VALUES (?,?,?,?,?)")
        val createdAt = ZonedDateTime.now(zoneId)

        val userId: Option[Int] = user match {
            case None => None
            case Some(u) => Some(u.id)
        }
        insertSQL.bind(createdAt, userId, login, ip, succeeded).update.apply()
    }

    def update(user: Option[User], login: String, ip: String, succeeded: Try[String]) {
        val succeededVal: Int = succeeded match {
            case Success(s) => 1
            case _ => 0
        }
        update(user, login, ip, succeededVal)
    }

    def getLastLoginById(id: Int): LoginLog = {
        val sql = SQL("SELECT * FROM login_log WHERE succeeded = 1 AND user_id = ? ORDER BY id DESC LIMIT 2")
        val logs: List[LoginLog] = sql.bind(id).map(loginLogMapping).list.apply()

        logs.last
    }

    def getFailuresCountByIp(ip: String): Int = {
        val sql = SQL("SELECT COUNT(1) AS failures FROM login_log WHERE " +
            "ip = ? AND id > IFNULL((select id from login_log where ip = ? AND " +
            "succeeded = 1 ORDER BY id DESC LIMIT 1), 0)")

        sql.bind(ip, ip).map(rs => rs.int("failures")).single.apply() match {
            case None => 0
            case Some(x) => x
        }
    }

    def getFailuresCountById(id: Int): Int = {
        val sql = SQL("SELECT COUNT(1) AS failures FROM login_log WHERE " +
            " user_id = ? AND id > IFNULL((select id from login_log where user_id = ? " +
            " AND succeeded = 1 ORDER BY id DESC LIMIT 1), 0)")

        sql.bind(id, id).map(rs => rs.int("failures")).single.apply() match {
            case None => 0
            case Some(x) => x
        }
    }

    def getBannedIPList(threshold: Int): List[String] = {
        val sql = SQL("SELECT ip FROM " +
            " (SELECT ip, MAX(succeeded) as max_succeeded, COUNT(1) as cnt FROM login_log GROUP BY ip) AS t0 " +
            " WHERE t0.max_succeeded = 0 AND t0.cnt >= ?")

        sql.bind(threshold).map(rs => rs.string("ip")).list.apply()
    }

    def getLastLoginIPList: List[LastLoginIP] = {
        val sql = SQL("SELECT ip, MAX(id) AS last_login_id FROM login_log WHERE succeeded = 1 GROUP by ip")
        
        sql.map(lastLoginIPMapping).list.apply()
    }

    def getFailuresIPCountByLastLogin(lastLoginIP: LastLoginIP): Int = {
        val sql = SQL("SELECT COUNT(1) AS cnt FROM login_log WHERE ip = ? AND ? < id")

        sql.bind(lastLoginIP.ip, lastLoginIP.lastLoginId).map(rs => rs.int("cnt")).single.apply() match {
            case None => 0
            case Some(x) => x
        }
    }

    def getLockedUserNameList(threshold: Int): List[String] = {
        val sql = SQL("SELECT user_id, login FROM " +
            " (SELECT user_id, login, MAX(succeeded) as max_succeeded, COUNT(1) as cnt FROM login_log GROUP BY user_id) AS t0 " +
            " WHERE t0.user_id IS NOT NULL AND t0.max_succeeded = 0 AND t0.cnt >= ?")

        sql.bind(threshold).map(rs => rs.string("login")).list.apply()
    }

    def getLastLoginNameList: List[LastLoginName] = {
        val sql = SQL("SELECT user_id, login, MAX(id) AS last_login_id FROM login_log WHERE user_id IS NOT NULL AND succeeded = 1 GROUP BY user_id")

        sql.map(lastLoginNameMapping).list.apply()
    }


    def getFailuresNameCountByLastLoginName(lastLoginName: LastLoginName): Int = {
        val sql = SQL("SELECT COUNT(1) AS cnt FROM login_log WHERE user_id = ? AND ? < id")

        sql.bind(lastLoginName.userId, lastLoginName.lastLoginId).map(rs => rs.int("cnt")).single.apply() match {
            case None => 0
            case Some(x) => x
        }
    }
}
