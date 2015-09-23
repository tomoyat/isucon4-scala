package models

case class User (id: Long, login: String, password_hash: String, salt: String)
case class DisplayUser(login: String, ip: String, createdAt: String)