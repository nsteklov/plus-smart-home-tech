package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.*;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.configuration.KafkaPropertiesConfig;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class KafkaProducerService implements AutoCloseable {

    private final KafkaPropertiesConfig propertiesConfig;
    private Producer<String, SpecificRecordBase> producer;
    EnumMap<TopicType, String> topics;

    public KafkaProducerService(KafkaPropertiesConfig propertiesConfig) {
        this.propertiesConfig = propertiesConfig;
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, propertiesConfig.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, propertiesConfig.getKeySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, propertiesConfig.getValueSerializer());
        topics =  new EnumMap<>(TopicType.class);
        topics.put(TopicType.SENSOR_EVENTS, propertiesConfig.getSensorEventTopic());
        topics.put(TopicType.HUB_EVENTS, propertiesConfig.getHubEventTopic());
        producer = new KafkaProducer<>(config);
    }

    public <T extends SpecificRecordBase> void send(T value, String key, Instant timestamp, TopicType topicType) {
        String topic = topics.get(topicType);
        if (topic == null) {
            throw new IllegalArgumentException("Неизвестный топик" );
        }
        ProducerRecord producerRecord = new ProducerRecord<>(topic, null, timestamp.toEpochMilli(), key, value);
        Future<RecordMetadata> future = producer.send(producerRecord);
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // Превышено время ожидания
            log.error("Превышено время ожидания");
            future.cancel(true); // Отменяем задачу
        } catch (InterruptedException | ExecutionException e) {
            log.error("Возникла ошибка при отправке сообщения: ", e);
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
