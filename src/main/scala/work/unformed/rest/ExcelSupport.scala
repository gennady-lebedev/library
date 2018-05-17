package work.unformed.rest

import java.io.ByteArrayOutputStream

import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes.attachment
import akka.http.scaladsl.model.headers.`Content-Disposition`
import akka.util.ByteString
import com.norbitltd.spoiwo.model.{Row, Sheet}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import work.unformed.rest.meta.{Meta, Result}

import scala.collection.immutable

trait ExcelSupport {

  def excelMarshaller[T <: Product : Meta]: ToResponseMarshaller[Result[T]] =
    Marshaller.withFixedContentType(MediaTypes.`application/excel`) { result =>
      HttpResponse(
        StatusCodes.OK,
        immutable.Seq(`Content-Disposition`(attachment, Map("filename" -> (implicitly[Meta[T]].typeName + ".csv")))),
        HttpEntity.Strict(
          ContentType(MediaTypes.`application/excel`),
          ByteString(asExcel(result.result).toByteArray)
        )
      )
    }

  private def asExcel[T <: Product : Meta](values: Seq[T]): ByteArrayOutputStream = {
    val stream = new ByteArrayOutputStream()
    Sheet(name = implicitly[Meta[T]].typeName)
      .withRows(values.map(i => Row().withCellValues(i.productIterator.toList)))
      .writeToOutputStream(stream)
    stream
  }
}
