package models

import java.time.ZonedDateTime

case class LoginLog(id: Long, created_at: ZonedDateTime, user_id: Option[Int], login: String, ip: String, succeeded: Int)
case class LastLoginIP(ip: String, lastLoginId: Long)
case class LastLoginName(userId: Int, login: String, lastLoginId: Long)