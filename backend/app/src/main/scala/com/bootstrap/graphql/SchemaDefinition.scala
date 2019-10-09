package com.bootstrap.graphql
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import com.bootstrap.graphql.schemas.{User}

import sangria.schema.{Schema, ObjectType}
import sangria.execution.FieldTag

import datamodel.latest.schema.tables.{User => UserTable, UserRow}
import com.bootstrap.dao.UserDao

object Fields {
  val query = ObjectType("Query",
    User.queries.fieldsFn() ++
      List()
  )

  val mutation = ObjectType("Mutation",
    User.mutations.fieldsFn() ++
      List()
  )
}

case object Authorised extends FieldTag

case class SecureContext(token: Option[String]) {
  private val userOpt : Option[UserRow] = token.flatMap(token => {
    Await.result(UserDao.getFromToken(token), Duration.Inf)
  })

  val userIsAuth = userOpt.isDefined

  lazy val user : UserRow = userOpt.get
}


object SchemaDefinition {
  val schema = Schema(Fields.query, Some(Fields.mutation))
  def secureContext(token: Option[String]) = new SecureContext(token)
}
