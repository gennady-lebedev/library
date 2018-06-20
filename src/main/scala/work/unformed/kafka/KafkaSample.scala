package work.unformed.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.kafka.ProducerMessage.MultiResultPart
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer, StringSerializer}

import scala.concurrent.{ExecutionContext, Future}

class KafkaSample(implicit sys: ActorSystem, mat: ActorMaterializer) extends LazyLogging {
  val config: Config = ConfigFactory.defaultApplication()
  implicit val ec: ExecutionContext = sys.dispatcher

  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(config.getConfig("akka.kafka.producer"), new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(config.getConfig("akka.kafka.consumer"), new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val done: Future[String] = Source(1 to 100)
    .map { number =>
      val value = number.toString
      ProducerMessage.Message(
        new ProducerRecord("topic1", 0, "key", value),
        number
      )
    }
    .via(Producer.flexiFlow(producerSettings))
    .map {
      case ProducerMessage.Result(metadata, message) =>
        val record = message.record
        s"Result ${metadata.topic}/${metadata.partition} ${metadata.offset}: ${record.value}"

      case ProducerMessage.MultiResult(parts, passThrough) =>
        parts
          .map {
            case MultiResultPart(metadata, record) =>
              s"Multipart ${metadata.topic}/${metadata.partition} ${metadata.offset}: ${record.value}"
          }
          .mkString(", ")

      case ProducerMessage.PassThroughResult(passThrough) =>
        s"passed through"
    }
    .runWith(Sink.last)

  val control =
    Consumer
      .atMostOnceSource(consumerSettings, Subscriptions.topics("topic1"))
      .mapAsync(10){ record =>
        logger.info(s"Done with #${record.offset()}")
        Future(record.value())
      }
      .to(Sink.foreach(it => logger.info(s"Done with $it")))
      .run()
}
