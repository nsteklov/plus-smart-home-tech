package ru.yandex.practicum.service.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.service.KafkaProducerService;

@Component(value = "DEVICE_ADDED")
public class DeviceAddedHubEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {

    public DeviceAddedHubEventHandler(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEventProto event) {
        DeviceAddedEventProto _event = event.getDeviceAdded();
        DeviceTypeAvro deviceTypeAvro = null;
        switch(_event.getType()){
            case DeviceTypeProto.CLIMATE_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.CLIMATE_SENSOR;
                break;
            case DeviceTypeProto.MOTION_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.MOTION_SENSOR;
                break;
            case DeviceTypeProto.LIGHT_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.LIGHT_SENSOR;
                break;
            case DeviceTypeProto.TEMPERATURE_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.TEMPERATURE_SENSOR;
                break;
            case DeviceTypeProto.SWITCH_SENSOR:
                deviceTypeAvro = DeviceTypeAvro.SWITCH_SENSOR;
                break;
        }
        if(deviceTypeAvro == null) {
            throw new IllegalArgumentException("Не найден тип устройства в avro - схеме для типа: " + _event.getType());
        }
        return DeviceAddedEventAvro.newBuilder()
                .setId(_event.getId())
                .setType(deviceTypeAvro)
                .build();
    }
}
