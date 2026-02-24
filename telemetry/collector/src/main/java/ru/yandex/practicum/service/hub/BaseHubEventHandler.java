package ru.yandex.practicum.service.hub;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.model.HubEvent;
import ru.yandex.practicum.service.KafkaProducerService;

@Slf4j
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {
    private final KafkaProducerService kafkaProducerService;

    public BaseHubEventHandler(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    protected abstract T mapToAvro(HubEvent event);

    public void handle(HubEvent event)  {

        if (!event.getType().equals(getMessageType())) {
            throw new IllegalArgumentException("Неизвестный тип события: " + event.getType());
        }

        T payload = mapToAvro(event);

        HubEventAvro eventAvro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();

        kafkaProducerService.send(eventAvro);
        log.info("отправлено сообщение: {}", eventAvro.toString());
    }

}
