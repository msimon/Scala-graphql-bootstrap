package com.bootstrap.dao

import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.MySQLProfile.api._

import com.bootstrap.DB
import datamodel.latest.schema.tables.{User => UserTable, UserRow, Token => TokenTable}
import org.mindrot.jbcrypt.BCrypt
import java.sql.Timestamp
import java.util.Calendar

object UserDao {
  def now() = {
    new Timestamp(Calendar.getInstance().getTime().getTime());
  }

  def get(pk: Int) = {
    val q = UserTable.filter(_.id === pk).take(1)
    DB.instance.run(q.result.headOption)
  }

  def getFromToken(tokenValue: String) = {
    val q = TokenTable.join(UserTable).on(_.userId === _.id)
        .filter(_._1.token === tokenValue)
        .filter(_._1.expiresAt > now())
        .take(1)
        .map({case (t, u) => u})

    DB.instance.run(q.result.headOption)
  }

  def getAll() = {
    DB.instance.run(UserTable.result)
  }

  def createUser(email: String, password: String) = {
    val password_hash = BCrypt.hashpw(password, BCrypt.gensalt())

    val usersMap = UserTable.map(c => (c.email, c.passwordHash))
    val action = usersMap.returning(UserTable.map(_.id)) += (email, password_hash)

    DB.instance.run(action).flatMap(
      userId => {
        TokenDao.create(userId)
        get(userId)
      }
    )
  }
}
