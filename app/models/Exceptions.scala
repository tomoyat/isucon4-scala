package models

case class LoginException(message: String) extends Exception(message)