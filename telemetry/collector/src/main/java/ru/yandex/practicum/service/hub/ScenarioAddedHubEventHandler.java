package ru.yandex.practicum.service.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.service.KafkaProducerService;

import java.util.ArrayList;
import java.util.List;

@Component(value = "SCENARIO_ADDED")
public class ScenarioAddedHubEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedHubEventHandler(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto _event = event.getScenarioAdded();
        List<ScenarioConditionAvro> ScenarioConditionsAvro = new ArrayList<>();
        for (ScenarioConditionProto scenarioCondition : _event.getConditionList()) {
            ConditionTypeAvro conditionTypeAvro;
            switch(scenarioCondition.getType()){
                case ConditionTypeProto.MOTION:
                    conditionTypeAvro = ConditionTypeAvro.MOTION;
                    break;
                case ConditionTypeProto.LUMINOSITY:
                    conditionTypeAvro = ConditionTypeAvro.LUMINOSITY;
                    break;
                case ConditionTypeProto.SWITCH:
                    conditionTypeAvro = ConditionTypeAvro.SWITCH;
                    break;
                case ConditionTypeProto.TEMPERATURE:
                    conditionTypeAvro = ConditionTypeAvro.TEMPERATURE;
                    break;
                case ConditionTypeProto.CO2LEVEL:
                    conditionTypeAvro = ConditionTypeAvro.CO2LEVEL;
                    break;
                case ConditionTypeProto.HUMIDITY:
                    conditionTypeAvro = ConditionTypeAvro.HUMIDITY;
                    break;
                default:
                    throw new IllegalArgumentException("Не найден тип условия для условия: " + scenarioCondition);
            }
            ConditionOperationAvro conditionOperationAvro;
            switch(scenarioCondition.getOperation()){
                case ConditionOperationProto.EQUALS:
                    conditionOperationAvro = ConditionOperationAvro.EQUALS;
                    break;
                case ConditionOperationProto.LOWER_THAN:
                    conditionOperationAvro = ConditionOperationAvro.LOWER_THAN;
                    break;
                case ConditionOperationProto.GREATER_THAN:
                    conditionOperationAvro = ConditionOperationAvro.GREATER_THAN;
                    break;
                default:
                    throw new IllegalArgumentException("Не найден тип операции для условия: " + scenarioCondition);
            }
            ScenarioConditionAvro scenarioConditionAvro = ScenarioConditionAvro.newBuilder()
                    .setSensorId(scenarioCondition.getSensorId())
                    .setType(conditionTypeAvro)
                    .setOperation(conditionOperationAvro)
                    .setValue(scenarioCondition.getValueCase())
                    .build();
            ScenarioConditionsAvro.add(scenarioConditionAvro);
        }

        List<DeviceActionAvro> deviceActionsAvro = new ArrayList<>();
        for (DeviceActionProto deviceAction : _event.getActionList()) {
            ActionTypeAvro actionTypeAvro;
            switch(deviceAction.getType()){
                case ActionTypeProto.ACTIVATE:
                    actionTypeAvro = ActionTypeAvro.ACTIVATE;
                    break;
                case ActionTypeProto.DEACTIVATE:
                    actionTypeAvro = ActionTypeAvro.DEACTIVATE;
                    break;
                case ActionTypeProto.INVERSE:
                    actionTypeAvro = ActionTypeAvro.INVERSE;
                    break;
                case ActionTypeProto.SET_VALUE:
                    actionTypeAvro = ActionTypeAvro.SET_VALUE;
                    break;
                default:
                    throw new IllegalArgumentException("Не найден тип действия для действия: " + deviceAction);
            }
            DeviceActionAvro deviceActionAvro = DeviceActionAvro.newBuilder()
                    .setSensorId(deviceAction.getSensorId())
                    .setType(actionTypeAvro)
                    .setValue(deviceAction.getValue())
                    .build();
            deviceActionsAvro.add(deviceActionAvro);
        }

        return ScenarioAddedEventAvro.newBuilder()
                .setName(_event.getName())
                .setConditions(ScenarioConditionsAvro)
                .setActions(deviceActionsAvro)
                .build();
    }
}
