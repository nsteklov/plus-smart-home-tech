package ru.yandex.practicum.service.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.HubEvent;
import ru.yandex.practicum.model.HubEventType;

public interface HubEventHandler {
    HubEventProto.PayloadCase getMessageType();

    void handle(HubEventProto event);
}
