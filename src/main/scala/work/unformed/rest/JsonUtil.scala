package work.unformed.rest

import io.circe.Decoder.Result
import io.circe._
import work.unformed.rest.meta.{Asc, Desc, Filter, Sort}
import java.sql.{Date, Time, Timestamp}

import scala.reflect.runtime.universe._

trait JsonUtil {
  implicit val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)


  implicit val timestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeString.apply(a.toString)
    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeString.map(Timestamp.valueOf).apply(c)
  }

  implicit val filterEncoder: Encoder[Filter] = new Encoder[Filter] {
    override def apply(a: Filter): Json = Encoder.encodeString.apply(a.toString)
  }

  implicit val sortEncoder: Encoder[Sort] = new Encoder[Sort] {
    override def apply(a: Sort): Json = Encoder.encodeString.apply(a.order match {
      case Desc => "!" + a.field
      case Asc => a.field
    })
  }

  implicit val typeEncoder: Encoder[Type] = new Encoder[Type] {
    override def apply(t: Type): Json = Encoder.encodeString(
    if (t =:= typeOf[String]) "string"
    else if (t =:= typeOf[Int] || t =:=  typeOf[Double] || t =:=  typeOf[Long] || t =:= typeOf[BigDecimal]) "number"
    else if (t =:= typeOf[Timestamp]) "timestamp"
    else if (t =:= typeOf[Date]) "date"
    else if (t =:= typeOf[Time]) "time"
    else "unknown"
    )
  }
}
