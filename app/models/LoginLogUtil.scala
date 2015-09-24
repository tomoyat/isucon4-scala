package models

import repositories.LoginLogs

object LoginLogUtil {

    val ipBanThreshold = 10

    def isIPBanned(ip: String): Boolean = {
        val cnt = LoginLogs.getFailuresCountByIp(ip)

        if (ipBanThreshold <= cnt) {
            return true
        }
        false
    }
}
