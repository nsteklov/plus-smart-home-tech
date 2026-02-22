package ru.yandex.practicum.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.model.*;

@Component(value = "DEVICE_REMOVED")
public class DeviceRemovedHubEventHandler extends BaseHubEventHandler<DeviceRemovedEventAvro> {
    public DeviceRemovedHubEventHandler(EventClient client) {
        super(client);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @Override
    protected DeviceRemovedEventAvro mapToAvro(HubEvent event) {
        DeviceRemovedEvent _event = (DeviceRemovedEvent) event;

        return DeviceRemovedEventAvro.newBuilder()
                .setId(_event.getId())
                .build();
    }
}
