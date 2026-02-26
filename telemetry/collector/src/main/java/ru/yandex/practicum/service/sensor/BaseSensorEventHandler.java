package ru.yandex.practicum.service.sensor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.model.SensorEvent;
import ru.yandex.practicum.service.KafkaProducerService;
import ru.yandex.practicum.service.TopicType;

@Slf4j
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {
    private final KafkaProducerService kafkaProducerService;

    public BaseSensorEventHandler(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    protected abstract T mapToAvro(SensorEvent event) ;

    public void handle(SensorEvent event) {
        if (!event.getType().equals(getMessageType())) {
            throw new IllegalArgumentException("Неизвестный тип события: " + event.getType());
        }

        T payload = mapToAvro(event);

        SensorEventAvro eventAvro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();

        kafkaProducerService.send(eventAvro, event.getHubId(), event.getTimestamp(), TopicType.SENSOR_EVENTS);
        log.info("отправлено сообщение: {}", eventAvro.toString());
    }
}
