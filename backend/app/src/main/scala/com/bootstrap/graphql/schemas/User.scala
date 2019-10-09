package com.bootstrap.graphql.schemas

import java.sql.SQLIntegrityConstraintViolationException
import scala.concurrent.ExecutionContext.Implicits.global

import sangria.schema.{ObjectType, Context, Field, StringType, OptionType}
import sangria.macros.derive.{deriveObjectType, deriveContextObjectType, GraphQLField, GraphQLFieldTags}
import sangria.macros.derive.{AddFields, ExcludeFields}

import com.bootstrap.graphql.exceptions.DuplicateException
import com.bootstrap.graphql.Utils.TimestampType
import com.bootstrap.graphql.{SecureContext, Authorised}

import datamodel.latest.schema.tables.{UserRow, TokenRow}
import com.bootstrap.dao.{UserDao, TokenDao}

object User {
  implicit val TokenType = deriveObjectType[SecureContext, TokenRow]()

  implicit val UserType = deriveObjectType[SecureContext, UserRow](
    ExcludeFields("passwordHash"),
    AddFields(
      Field("token", OptionType(TokenType), resolve = {
        ctx => TokenDao.getTokenValueByUserId(ctx.value.id)
      })
    )
  )

  object Queries {
    @GraphQLField
    @GraphQLFieldTags(Authorised)
    def user(ctx: Context[SecureContext, _]) = {
      ctx.ctx.user
    }
  }

  object Mutations {
    @GraphQLField
    def createUser(email: String, password: String) = {
      UserDao.createUser(email, password).transform(
        res => res,
        {
          case e : SQLIntegrityConstraintViolationException => {
            new DuplicateException(message=s"Duplicated email ${email}", cause=e)
          }
          case e => e
        }
      )
    }
  }

  val queries = deriveContextObjectType[SecureContext, Queries.type, Unit](_ => Queries)
  val mutations = deriveContextObjectType[SecureContext, Mutations.type, Unit](_ => Mutations)
}
