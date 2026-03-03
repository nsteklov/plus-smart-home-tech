package ru.yandex.practicum.service.hub;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.model.HubEvent;
import ru.yandex.practicum.service.KafkaProducerService;
import ru.yandex.practicum.service.TopicType;

import java.time.Instant;

@Slf4j
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {
    private final KafkaProducerService kafkaProducerService;

    public BaseHubEventHandler(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    protected abstract T mapToAvro(HubEventProto event);

    @Override
    public void handle(HubEventProto event)  {

        if (!event.getPayloadCase().equals(getMessageType())) {
            throw new IllegalArgumentException("Неизвестный тип события: " + event.getPayloadCase());
        }

        T payload = mapToAvro(event);

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        HubEventAvro eventAvro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();

        kafkaProducerService.send(eventAvro, event.getHubId(), timestamp, TopicType.HUB_EVENTS);
        log.info("отправлено сообщение: {}", eventAvro.toString());
    }

}
