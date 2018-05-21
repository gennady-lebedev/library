package work.unformed.kafka

import java.util.concurrent.Executors
import java.util.{Properties, UUID}

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.collection.JavaConverters._

trait KafkaSupport extends LazyLogging {
  val producer = new KafkaProducer[String, String](producerConfig())
  val id: String = UUID.randomUUID().toString

  private def producerConfig(): Properties = {
    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    properties
  }

  def send(value: Any, topic: String): Unit = {
    producer.send(new ProducerRecord(topic, id, value.toString))
  }

  val consumer = new KafkaConsumer[String, String](consumerConfig())

  private def consumerConfig(): Properties = {
    val properties = new Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "1")
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties
  }

  def receive(topic: String): Unit = {
    consumer.subscribe(Seq(topic).asJavaCollection)

    Executors.newSingleThreadExecutor.execute(() => {
      while (true) {
        val records = consumer.poll(1000)

        records.forEach { record =>
          logger.info("Received {}:{}", record.key(), record.value())
        }
      }
    })
  }
}
