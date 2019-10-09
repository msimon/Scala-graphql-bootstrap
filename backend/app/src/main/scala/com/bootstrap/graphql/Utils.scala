package com.bootstrap.graphql

import java.sql.Timestamp
import scala.util.{Failure, Success, Try}
import sangria.schema.ScalarType
import sangria.validation.{Violation, ValueCoercionViolation}
import sangria.ast._

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat

object Utils {
    // TiemStamp coercion
    case object TimestampCoerceViolation extends ValueCoercionViolation("Timestamp value expected")

    def parseDateStringToTime(s: String) = Try(new DateTime(s, DateTimeZone.UTC)) match {
        case Success(date) => Right(new Timestamp(date.getMillis()))
        case Failure(_) => Left(TimestampCoerceViolation)
    }

    implicit val TimestampType = ScalarType[Timestamp](
        "Timestamp",
        coerceOutput = (ts, _) => new DateTime(ts.getTime).toString(),
        coerceInput = {
            case StringValue(ts, _, _, _,_) => parseDateStringToTime(ts)
            case _ => Left(TimestampCoerceViolation)
        },
        coerceUserInput = {
            case s: String => parseDateStringToTime(s)
            case _ => Left(TimestampCoerceViolation)
        })


    // DateTime coercion
    case object DateCoercionViolation extends ValueCoercionViolation("Date value expected")

    def parseDate(s: String) = Try(new DateTime(s, DateTimeZone.UTC)) match {
        case Success(date) => Right(date)
        case Failure(_) => Left(DateCoercionViolation)
    }

    val DateTimeType = ScalarType[DateTime](
        "DateTime",
        coerceOutput = (date, _) => StringValue(ISODateTimeFormat.dateTime().print(date)),
        coerceInput = {
            case StringValue(ts, _, _, _,_) => parseDate(ts)
            case _ => Left(DateCoercionViolation)
        },
        coerceUserInput = {
            case s: String => parseDate(s)
            case _ => Left(DateCoercionViolation)
        }
    )
}

