package models

case class Report(bannedIps: Seq[String], lockedUsers: Seq[String])