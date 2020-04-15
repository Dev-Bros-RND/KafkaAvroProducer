package org;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.pojo.Event;
import org.pojo.Priority;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.IntStream;

public class App {

    private static final SecureRandom random = new SecureRandom();

    public static void main(String ...args) {

        final String TOPIC = "avro_topic_example";

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, "10");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        Producer<String, Event> producer = new KafkaProducer<String, Event>(props);

        IntStream.range(1, 20_000).mapToObj(index ->
                Event.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setEventDate(new Date().getTime())
                        .setDescription("This is a description of the event")
                        .setPriority(randomPriority())
                        .build()
        ).map(event -> new ProducerRecord<String, Event>(TOPIC, event.getId(), event))
        .forEach(record -> producer.send(record,
                (metadata, exception) -> {
                    if(exception == null) System.out.println(metadata);
                    else System.out.println(exception.getMessage());
                }
            )
        );

        producer.flush();
        producer.close();

    }

    private static Priority randomPriority() {
        switch (random.nextInt(3)) {
            case 0: return Priority.LOW;
            case 1: return Priority.MEDIUM;
            default: return Priority.HIGH;
        }
    }

}
