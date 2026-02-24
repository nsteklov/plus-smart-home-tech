package ru.yandex.practicum.service.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.model.SensorEvent;
import ru.yandex.practicum.model.SensorEventType;
import ru.yandex.practicum.model.SwitchSensorEvent;
import ru.yandex.practicum.service.KafkaProducerService;

@Component(value = "SWITCH_SENSOR_EVENT")
public class SwitchSensorEventHandler extends BaseSensorEventHandler<SwitchSensorAvro> {

    public SwitchSensorEventHandler(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    protected SwitchSensorAvro mapToAvro(SensorEvent event) {
        SwitchSensorEvent _event = (SwitchSensorEvent) event;

        return SwitchSensorAvro.newBuilder()
                .setState(_event.isState())
                .build();
    }
}
