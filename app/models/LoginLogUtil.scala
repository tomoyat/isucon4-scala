package models

import repositories.LoginLogs

object LoginLogUtil {

    val ipBanThreshold = 10
    val userLockThreshold = 3

    def isIPBanned(ip: String): Boolean = {
        val cnt = LoginLogs.getFailuresCountByIp(ip)

        if (ipBanThreshold <= cnt) {
            return true
        }
        false
    }

    def isLocked(user: Option[User]): Boolean = {
        user match {
            case None => false
            case Some(u) => {
                LoginLogs.getFailuresCountById(u.id) match {
                    case x if x >= userLockThreshold => true
                    case _ => false
                }
            }
        }
    }
}
