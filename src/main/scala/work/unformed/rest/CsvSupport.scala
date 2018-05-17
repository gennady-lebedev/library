package work.unformed.rest

import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes.attachment
import akka.http.scaladsl.model.headers.`Content-Disposition`
import akka.stream.scaladsl.Source
import akka.util.ByteString
import work.unformed.rest.meta.{Meta, Result}

import scala.collection.immutable

trait CsvSupport {
  def csvMarshaller[T <: Product : Meta]: ToResponseMarshaller[Result[T]] =
    Marshaller.withOpenCharset(MediaTypes.`text/csv`) { (result, charset) =>
      HttpResponse(
        StatusCodes.OK,
        immutable.Seq(`Content-Disposition`(attachment, Map("filename" -> (implicitly[Meta[T]].typeName + ".csv")))),
        HttpEntity.CloseDelimited(
          ContentType(MediaTypes.`text/csv`, charset),
          Source.fromIterator { () => result.result.map(asCSV).iterator}.map(s => ByteString(s, charset.nioCharset()))
        )
      )
    }

  private def asCSV[T <: Product](value: T): String = {
    value.productIterator.mkString(";") + "\n"
  }

}
