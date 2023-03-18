package com.portx.payment.messaging;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Service
public class KafkaService {

    private static final String BOOTSTRAP_SERVERS = "broker:29092";
    private KafkaProducer<String, String> producer;

    @PostConstruct
    private void init() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(properties);
    }

    public void publishEvent(String topic, String value) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, value);
        producer.send(producerRecord);
        producer.flush();
        producer.close();
    }
}
