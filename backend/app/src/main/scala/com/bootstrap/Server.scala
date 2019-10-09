package com.bootstrap

import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.immutable
import com.typesafe.config.ConfigFactory
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

import sangria.ast.{Document, StringValue}
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError, HandledException, ExceptionHandler}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.parser.DeliveryScheme.Try
import sangria.marshalling.circe._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server._
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._

import com.bootstrap.graphql.{SchemaDefinition, SecureContext, AuthMiddleware}
import com.bootstrap.exceptions.CommonException


object Server extends App {
  implicit val system = ActorSystem("bootstrap-server")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val serverConfig = ConfigFactory.load().getConfig("server")

  val defaultHeaders = immutable.Seq(
    RawHeader("Access-Control-Allow-Origin", serverConfig.getString("headers.ac-allow-origin")),
    RawHeader("Access-Control-Allow-Methods", serverConfig.getString("headers.ac-allow-methods")),
    RawHeader("Access-Control-Allow-Headers", serverConfig.getString("headers.ac-allow-headers")),
    RawHeader("Access-Control-Allow-Credentials", serverConfig.getString("headers.ac-allow-credentials"))
  )

  val exceptionHandler : sangria.execution.ExceptionHandler = sangria.execution.ExceptionHandler(
    onException = {
      case (m, e: CommonException) => HandledException(
        e.getMessage,
        Map("key" -> m.scalarNode(e.getKey, "String", Set.empty))
      )
      case (_, e: Exception) => HandledException(e.getMessage) // TODO only keep this in dev mode
    }
  )

  def executeGraphQL(query: Document, operationName: Option[String], variables: Json, token: Option[String]) =
    Executor.execute(
      schema = SchemaDefinition.schema,
      queryAst = query,
      userContext = SchemaDefinition.secureContext(token),
      middleware = AuthMiddleware :: Nil,
      variables = if (variables.isNull) Json.obj() else variables,
      operationName = operationName,
      exceptionHandler = exceptionHandler
    ).map(
      OK -> _
    ).recover {
      case error: QueryAnalysisError => BadRequest -> error.resolveError
      case error: ErrorWithResolver => InternalServerError -> error.resolveError
    }

  def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError =>
      Json.obj("errors" -> Json.arr(
        Json.obj(
          "message" -> Json.fromString(syntaxError.getMessage),
          "locations" -> Json.arr(Json.obj(
            "line" -> Json.fromBigInt(syntaxError.originalError.position.line),
            "column" -> Json.fromBigInt(syntaxError.originalError.position.column))))))
    case NonFatal(e) =>
      formatError(e.getMessage)
    case e =>
      throw e
  }

  def formatError(message: String): Json =
    Json.obj("errors" -> Json.arr(Json.obj("message" -> Json.fromString(message))))

  val route: Route =
    path("graphql") {
      post {
        respondWithHeaders(responseHeaders=defaultHeaders) {
          optionalCookie("token") { tokenCookie => {
            val token = tokenCookie.map(_.value)
            parameters('query.?, 'operationName.?, 'variables.?) { (queryParam, operationNameParam, variablesParam) =>
              entity(as[Json]) { body =>
                val query = queryParam orElse root.query.string.getOption(body)
                val operationName = operationNameParam orElse root.operationName.string.getOption(body)
                val variablesStr = variablesParam orElse root.variables.string.getOption(body)

                query.map(QueryParser.parse(_)) match {
                  case Some(Success(ast)) =>
                    variablesStr.map(parse) match {
                      case Some(Left(error)) => complete(BadRequest, formatError(error))
                      case Some(Right(json)) => complete(executeGraphQL(ast, operationName, json, token))
                      case None => complete(
                        executeGraphQL(ast, operationName, root.variables.json.getOption(body) getOrElse Json.obj(), token)
                      )
                    }
                  case Some(Failure(error)) => complete(BadRequest, formatError(error))
                  case None => complete(BadRequest, formatError("No query to execute"))
                }
              }
            }
          }}
        }
      } ~ options {
        respondWithHeaders(responseHeaders=defaultHeaders) {
          complete("")
        }
      }
    }

  val hostname = serverConfig.getString("hostname")
  val port = serverConfig.getInt("http-port")
  Http().bindAndHandle(route, hostname)
  println(s"Server online at http://${hostname}:${port}")
}
