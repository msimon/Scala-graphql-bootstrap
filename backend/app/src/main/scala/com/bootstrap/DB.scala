package com.bootstrap

import slick.jdbc.MySQLProfile.api.Database

import scala.util.{Failure, Success, Try}

object DB {
  val instance = Database.forConfig("slick.db")
}
