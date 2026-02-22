package ru.yandex.practicum.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.model.MotionSensorEvent;
import ru.yandex.practicum.model.SensorEvent;
import ru.yandex.practicum.model.SensorEventType;

@Component(value = "MOTION_SENSOR_EVENT")
public class MotionSensorEventHandler extends BaseSensorEventHandler<MotionSensorAvro> {
    public MotionSensorEventHandler(EventClient client) {
        super(client);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    protected MotionSensorAvro mapToAvro(SensorEvent event) {
        MotionSensorEvent _event = (MotionSensorEvent) event;

        return MotionSensorAvro.newBuilder()
                .setLinkQuality(_event.getLinkQuality())
                .setMotion(_event.isMotion())
                .setVoltage(_event.getVoltage())
                .build();
    }
}
