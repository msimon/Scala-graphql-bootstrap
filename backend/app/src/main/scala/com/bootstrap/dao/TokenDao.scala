package com.bootstrap.dao

import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.MySQLProfile.api._

import com.bootstrap.DB
import datamodel.latest.schema.tables.{Token => TokenTable}

import java.sql.Timestamp
import java.util.Calendar
import java.util.UUID.randomUUID

object TokenDao {
  def create(userId: Int) = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DATE, +7);

    val token = randomUUID().toString
    val expiresAt = new Timestamp(calendar.getTime().getTime())

    val action = TokenTable.map(c => (c.userId, c.token, c.expiresAt)) += (userId, token, expiresAt)

    DB.instance.run(action)
  }

  def getTokenValueByUserId(userId: Int) = {
    val q = TokenTable.filter(_.userId === userId)
        .sortBy(_.expiresAt.desc)
        .take(1)

    DB.instance.run(q.result.headOption)
  }
}

