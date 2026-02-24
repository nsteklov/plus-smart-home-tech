package ru.yandex.practicum.service.sensor;

import ru.yandex.practicum.model.SensorEvent;
import ru.yandex.practicum.model.SensorEventType;

public interface SensorEventHandler {
    SensorEventType getMessageType();

    void handle(SensorEvent event);
}
