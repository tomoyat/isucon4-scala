package repositories

import scalikejdbc._
object Users {
    implicit val session = AutoSession
    def getUser: Unit = {
        val result = SQL("select * from users limit 10").toMap().list.apply()
        println(result.toString())
    }
}
