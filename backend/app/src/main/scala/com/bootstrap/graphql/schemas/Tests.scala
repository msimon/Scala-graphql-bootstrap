package com.bootstrap.graphql.schemas

import scala.util.Random
import scala.collection.mutable.Map

import sangria.macros.derive._
import sangria.schema._
import sangria.execution.FieldTag


import com.bootstrap.graphql.{SecureContext, Authorised}


object TestGraphql {
  case class Person(name: String)
  implicit val PersonType = deriveObjectType[SecureContext, Person]()

  object Queries {

    // @GraphQLField
    // def helloWorld(ctx: Context[SecureContext, _], name: String) = {
    //   Person(name)
    // }

    // @GraphQLField
    // @GraphQLFieldTags(Authorised)
    // def helloWorldSecure(ctx: Context[SecureContext, _]) = {
    //   val user = ctx.ctx.user
    //   Person(user.name)
    // }
  }

  // val queries = deriveContextObjectType[SecureContext, Queries.type, Unit](_ => Queries)
}
