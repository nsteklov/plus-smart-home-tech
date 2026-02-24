package ru.yandex.practicum.service.hub;

import org.springframework.stereotype.Component;
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
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEvent event) {
        ScenarioAddedEvent _event = (ScenarioAddedEvent) event;
        System.out.println(_event);
        List<ScenarioConditionAvro> ScenarioConditionsAvro = new ArrayList<>();
        for (ScenarioCondition scenarioCondition : _event.getConditions()) {
            ConditionTypeAvro conditionTypeAvro;
            switch(scenarioCondition.getType()){
                case ConditionType.MOTION:
                    conditionTypeAvro = ConditionTypeAvro.MOTION;
                    break;
                case ConditionType.LUMINOSITY:
                    conditionTypeAvro = ConditionTypeAvro.LUMINOSITY;
                    break;
                case ConditionType.SWITCH:
                    conditionTypeAvro = ConditionTypeAvro.SWITCH;
                    break;
                case ConditionType.TEMPERATURE:
                    conditionTypeAvro = ConditionTypeAvro.TEMPERATURE;
                    break;
                case ConditionType.CO2LEVEL:
                    conditionTypeAvro = ConditionTypeAvro.CO2LEVEL;
                    break;
                case ConditionType.HUMIDITY:
                    conditionTypeAvro = ConditionTypeAvro.HUMIDITY;
                    break;
                default:
                    throw new IllegalArgumentException("Не найден тип условия для условия: " + scenarioCondition);
            }
            ConditionOperationAvro conditionOperationAvro;
            switch(scenarioCondition.getOperation()){
                case ConditionOperation.EQUALS:
                    conditionOperationAvro = ConditionOperationAvro.EQUALS;
                    break;
                case ConditionOperation.LOWER_THAN:
                    conditionOperationAvro = ConditionOperationAvro.LOWER_THAN;
                    break;
                case ConditionOperation.GREATER_THAN:
                    conditionOperationAvro = ConditionOperationAvro.GREATER_THAN;
                    break;
                default:
                    throw new IllegalArgumentException("Не найден тип операции для условия: " + scenarioCondition);
            }
            ScenarioConditionAvro scenarioConditionAvro = ScenarioConditionAvro.newBuilder()
                    .setSensorId(scenarioCondition.getSensorId())
                    .setType(conditionTypeAvro)
                    .setOperation(conditionOperationAvro)
                    .setValue(scenarioCondition.getValue())
                    .build();
            ScenarioConditionsAvro.add(scenarioConditionAvro);
        }

        List<DeviceActionAvro> deviceActionsAvro = new ArrayList<>();
        for (DeviceAction deviceAction : _event.getActions()) {
            ActionTypeAvro actionTypeAvro;
            switch(deviceAction.getType()){
                case ActionType.ACTIVATE:
                    actionTypeAvro = ActionTypeAvro.ACTIVATE;
                    break;
                case ActionType.DEACTIVATE:
                    actionTypeAvro = ActionTypeAvro.DEACTIVATE;
                    break;
                case ActionType.INVERSE:
                    actionTypeAvro = ActionTypeAvro.INVERSE;
                    break;
                case ActionType.SET_VALUE:
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
