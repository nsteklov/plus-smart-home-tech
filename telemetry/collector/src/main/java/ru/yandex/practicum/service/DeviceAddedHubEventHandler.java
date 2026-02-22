package ru.yandex.practicum.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.model.*;

@Component(value = "DEVICE_ADDED")
public class DeviceAddedHubEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {

    public DeviceAddedHubEventHandler(EventClient client) {
        super(client);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_ADDED;
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEvent event) {
        DeviceAddedEvent _event = (DeviceAddedEvent) event;
        DeviceTypeAvro deviceTypeAvro = null;
        switch(_event.getDeviceType()){
            case DeviceType.CLIMATE_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.CLIMATE_SENSOR;
                break;
            case DeviceType.MOTION_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.MOTION_SENSOR;
                break;
            case DeviceType.LIGHT_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.LIGHT_SENSOR;
                break;
            case DeviceType.TEMPERATURE_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.TEMPERATURE_SENSOR;
                break;
            case DeviceType.SWITCH_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.SWITCH_SENSOR;
                break;
        }
        if(deviceTypeAvro == null) {
            throw new IllegalArgumentException("Не найден тип устройства в avro - схеме для типа: " + event.getType());
        }
        return DeviceAddedEventAvro.newBuilder()
                .setId(_event.getId())
                .setType(deviceTypeAvro)
                .build();
    }
}
