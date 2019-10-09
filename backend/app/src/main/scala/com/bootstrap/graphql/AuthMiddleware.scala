package com.bootstrap.graphql

import sangria.schema._
import sangria.execution._

import com.bootstrap.graphql.exceptions.NotAuthorizedException

object AuthMiddleware extends Middleware[SecureContext] with MiddlewareBeforeField[SecureContext] {
  type QueryVal = Unit
  type FieldVal = Unit

  def beforeQuery(context: MiddlewareQueryContext[SecureContext, _, _]) = ()
  def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[SecureContext, _, _]) = ()

  def beforeField(queryVal: QueryVal, mctx: MiddlewareQueryContext[SecureContext, _, _], ctx: Context[SecureContext, _]) = {
    val requireAuth = ctx.field.tags.contains(Authorised)
    val securityCtx = ctx.ctx

    if (requireAuth && securityCtx.userIsAuth == false) {
      throw new NotAuthorizedException
    }
    continue
  }
}
