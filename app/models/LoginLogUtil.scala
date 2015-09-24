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

    def getBannedIPs: List[String] = {
        var ipList = LoginLogs.getBannedIPList(ipBanThreshold)

        val lastLoginList = LoginLogs.getLastLoginIPList

        for (lastLogin <- lastLoginList) {
            val cnt = LoginLogs.getFailuresIPCountByLastLogin(lastLogin)
            if (ipBanThreshold <= cnt) {
                ipList = ipList ::: List(lastLogin.ip)
            }
        }
        ipList
    }

    def getLockedUserNames: List[String] = {

        var nameList = LoginLogs.getLockedUserNameList(userLockThreshold)

        val lastLoginList = LoginLogs.getLastLoginNameList

        for (lastLogin <- lastLoginList) {
            val cnt = LoginLogs.getFailuresNameCountByLastLoginName(lastLogin)
            if (userLockThreshold <= cnt) {
                nameList = nameList ::: List(lastLogin.login)
            }
        }
        nameList
    }
}
