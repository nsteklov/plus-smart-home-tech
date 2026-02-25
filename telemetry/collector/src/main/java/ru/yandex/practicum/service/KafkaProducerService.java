package ru.yandex.practicum.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.configuration.KafkaPropertiesConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.Properties;

@Service
public class KafkaProducerService implements AutoCloseable {

    private final KafkaPropertiesConfig propertiesConfig;
    private Producer<String, SpecificRecordBase> producer;

    public KafkaProducerService(KafkaPropertiesConfig propertiesConfig) {
        this.propertiesConfig = propertiesConfig;
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, propertiesConfig.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, propertiesConfig.getKeySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, propertiesConfig.getValueSerializer());
        producer = new KafkaProducer<>(config);
    }

    public <T extends org.apache.avro.specific.SpecificRecordBase> void send(T value) {
        String topic;
        if (value instanceof SensorEventAvro) {
            topic = propertiesConfig.getSensorEventTopic();
        } else if(value instanceof HubEventAvro) {
            topic = propertiesConfig.getHubEventTopic();
        } else {
            throw new IllegalArgumentException("Неизвестная avro-схема" );
        }
        try {
            producer.send(new ProducerRecord<>(topic, value));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
    }
}
