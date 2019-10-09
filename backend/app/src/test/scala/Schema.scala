import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


import org.scalatest.{Matchers, WordSpec}

import sangria.macros._
import sangria.execution._
import sangria.marshalling.circe._

import io.circe._
import io.circe.parser._

import com.dm.graphql.root.{SchemDefinition, MainContext, QueryRepo, MutationRepo}
import com.dm.graphql.schemas.TokenGraphql

object Conf {
  val ctx = new MainContext(query=new QueryRepo, mutation=new MutationRepo)
}

class Schema extends WordSpec with Matchers {
  "Token mutations" should {
    "be able to add a token" in {
      val mutation =
        graphql"""
          mutation addTokenMutation {
            addToken(value: "u832u9efuwj", provider: "slack") {
              id
              value
              provider
            }
          }"""

      val futureResult: Future[Json] =
        Executor.execute(SchemDefinition.schema, mutation, Conf.ctx)

      val result = Await.result(futureResult, 10.seconds)
    }
  }

  "Token queries" should {
    TokenGraphql.tokenHtlb += (
      "1234" -> TokenGraphql.Token(id="1234", provider="slack", value="11ncjkwbneiu"),
      "12335" -> TokenGraphql.Token(id="12335", provider="slack", value="231412pojiod")
    )

    "be able to fetch a token gicen an id" in {
      val query =
        graphql"""
          query getToken {
            token(id: "1234") {
              id
              provider
              value
          }
        }"""
      val futureResult: Future[Json] =
        Executor.execute(SchemDefinition.schema, query, Conf.ctx)

      val result = Await.result(futureResult, 10.seconds)

      result should be (parse(
        """{
          "data": {
            "token": {
              "id" : "1234",
              "provider" : "slack",
              "value" : "11ncjkwbneiu"
            }
          }
        }"""
      ).right.get)
    }
    "be able to fetch all tokens" in {
      val query =
        graphql"""
          query getTokens {
            tokens {
              id
              provider
              value
          }
        }"""
      val futureResult: Future[Json] =
        Executor.execute(SchemDefinition.schema, query, Conf.ctx)

      val result_json = Await.result(futureResult, 10.seconds)

      val resultOpt = result_json.hcursor.downField("data").downField("tokens").as[Seq[Json]] match {
        case Right(l) => Some(l.length)
        case Left(_) => None
      }
      resultOpt should be (Some(3))
    }
  }
}
