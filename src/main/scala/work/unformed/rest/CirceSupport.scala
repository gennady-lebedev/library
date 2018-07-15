package work.unformed.rest

import io.circe.Decoder.Result
import io.circe.generic.extras.{AutoDerivation, Configuration}
import io.circe._
import io.circe.syntax._
import work.unformed.meta._
import java.sql.{Date, Time, Timestamp}

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import work.unformed.library.item.ItemStatus
import work.unformed.library.model.UserRole
import work.unformed.meta.Meta.Field

import scala.reflect.runtime.universe._

trait CirceSupport extends AutoDerivation with FailFastCirceSupport {
  implicit val configuration: Configuration = Configuration.default.withDefaults
  implicit val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)


  implicit val timestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeString.apply(a.toString)
    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeString.map(Timestamp.valueOf).apply(c)
  }

  implicit val dateFormat: Encoder[Date] with Decoder[Date] = new Encoder[Date] with Decoder[Date] {
    override def apply(a: Date): Json = Encoder.encodeString.apply(a.toString)
    override def apply(c: HCursor): Result[Date] = Decoder.decodeString.map(Date.valueOf).apply(c)
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

  case class EntityMeta(entity: String, fields: Map[String, Field])

  implicit def metaEncoder[T <: Product]: Encoder[Meta[T]] = new Encoder[Meta[T]] {
    override def apply(a: Meta[T]): Json = Encoder.encodeJson{
      val f = a.fieldMap.flatMap {
        case (name, field) => Some(name, field)
        case _ => None
      }
      EntityMeta(a.typeName, f).asJson
    }
  }

  implicit val itemStatusFormat: Encoder[ItemStatus] with Decoder[ItemStatus] = new Encoder[ItemStatus] with Decoder[ItemStatus] {
    override def apply(a: ItemStatus): Json = Encoder.encodeString.apply(a.toString)
    override def apply(c: HCursor): Result[ItemStatus] = Decoder.decodeString.map(ItemStatus.valueOf).apply(c)
  }

  implicit val userRoleFormat: Encoder[UserRole] with Decoder[UserRole] = new Encoder[UserRole] with Decoder[UserRole] {
    override def apply(a: UserRole): Json = Encoder.encodeString.apply(a.toString)
    override def apply(c: HCursor): Result[UserRole] = Decoder.decodeString.map(UserRole.valueOf).apply(c)
  }
}
