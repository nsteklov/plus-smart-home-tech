package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.model.HubEvent;

@Slf4j
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {
    private final EventClient client;

    public BaseHubEventHandler(EventClient client) {
        this.client = client;
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

        client.getProducer().send(new ProducerRecord<>("telemetry.hubs.v1", eventAvro));
        log.info("отправлено сообщение: {}", eventAvro.toString());
    }

}
