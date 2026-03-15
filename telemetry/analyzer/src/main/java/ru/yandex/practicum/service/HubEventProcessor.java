package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaPropertiesConfigAnalyzer;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaPropertiesConfigAnalyzer propertiesConfig;
    private Consumer<String, HubEventAvro> consumer;
    private String hubEventTopic;
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    public HubEventProcessor(KafkaPropertiesConfigAnalyzer propertiesConfig, SensorRepository sensorRepository, ScenarioRepository scenarioRepository, ConditionRepository conditionRepository, ActionRepository actionRepository) {
        this.propertiesConfig = propertiesConfig;
        this.sensorRepository = sensorRepository;
        this.scenarioRepository = scenarioRepository;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;

        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, propertiesConfig.getClientIdHub());
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, propertiesConfig.getGroupIdHub());
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, propertiesConfig.getBootstrapServers());
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, propertiesConfig.getKeyDeserializer());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, propertiesConfig.getHubValueDeserializer());
        consumerConfig.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, propertiesConfig.getMaxPollRecordsConfig());
        consumerConfig.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, propertiesConfig.getFetchMaxBytesConfig());
        consumerConfig.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, propertiesConfig.getMaxPartitionFetchBytesConfig());
        consumer = new KafkaConsumer<>(consumerConfig);

        hubEventTopic = propertiesConfig.getHubEventTopic();
    }


    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            // подписываемся на топики
            List<String> topicsConsumer = new ArrayList<>();
            topicsConsumer.add(hubEventTopic);
            consumer.subscribe(topicsConsumer);

            // начинаем Poll Loop
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                int count = 0;
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    // обрабатываем очередную запись
                    try {
                        handleRecord(record);
                    } catch (Exception e) {
                        log.error("Возникла ошибка при обработке сообщения", e);
                    }
                    // фиксируем оффсеты обработанных записей, если нужно
                    manageOffsets(record, count, consumer);
                    count++;
                }
                // фиксируем максимальный оффсет обработанных записей
                consumer.commitAsync();
            }
        } catch (WakeupException ignores) {
            // Ничего здесь не делаем.
            // Закрываем консьюмер в finally блоке.
        } finally {
            // Перед закрытием консьюмера убеждаемся, что оффсеты обработанных сообщений
            // точно зафиксированы, вызываем для этого метод синхронной фиксации
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
//                log.info("Закрываем продюсер");
//                producer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, HubEventAvro> record, int count, Consumer<String, HubEventAvro> consumer) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<String, HubEventAvro> record) throws InterruptedException {
        log.info("Принимаем сообщение хаба, топик = {}, партиция = {}, смещение = {}, значение: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());
        HubEventAvro event = record.value();
        if (event.getPayload() instanceof DeviceAddedEventAvro) {
            createUpdateDevice((DeviceAddedEventAvro) event.getPayload(), event.getHubId());
        } else if (event.getPayload() instanceof DeviceRemovedEventAvro) {
            deleteDevice((DeviceRemovedEventAvro) event.getPayload());
        } else if (event.getPayload() instanceof ScenarioAddedEventAvro) {
            createUpdateScenario((ScenarioAddedEventAvro) event.getPayload(), event.getHubId());
        } else if (event.getPayload() instanceof ScenarioRemovedEventAvro) {
            deleteScenario((ScenarioRemovedEventAvro) event.getPayload(), event.getHubId());
        }
    }

    private void createUpdateDevice(DeviceAddedEventAvro deviceAddedEventAvro, String hubId) {
        if (!sensorRepository.existsById(deviceAddedEventAvro.getId())) {
            Sensor newSensor = new Sensor();
            newSensor.setId(deviceAddedEventAvro.getId());
            newSensor.setHubId(hubId);
            System.out.println("Вася " + newSensor);
            sensorRepository.save(newSensor);
            log.info("Записали новое устройство {}", deviceAddedEventAvro);
        } else {
            Sensor oldSensor = sensorRepository.findById(deviceAddedEventAvro.getId()).get();
            if (!oldSensor.getHubId().equals(hubId)) {
                oldSensor.setHubId(hubId);
                sensorRepository.save(oldSensor);
                log.info("Обновили устройство {}", deviceAddedEventAvro);
            }
        }
    }

    private void createUpdateScenario(ScenarioAddedEventAvro scenarioAddedEventAvro, String hubId) {
        Optional<Scenario> optScenario = scenarioRepository.findByHubIdAndName(hubId, scenarioAddedEventAvro.getName());
        Scenario scenario;
        if (optScenario.isEmpty()) {
            scenario = new Scenario();
            scenario.setHubId(hubId);
            scenario.setName(scenarioAddedEventAvro.getName());
        } else {
            scenario = optScenario.get();
        }
        Map<String, Condition> conditions = new HashMap<>();
        for (ScenarioConditionAvro scenarioConditionAvro : scenarioAddedEventAvro.getConditions()) {
            if (!sensorRepository.existsById(scenarioConditionAvro.getSensorId())) {
                throw new NotFoundException("Не найдено устройство с id: " + scenarioConditionAvro.getSensorId());
            }
            ConditionType conditionType;
            switch (scenarioConditionAvro.getType()) {
                case ConditionTypeAvro.MOTION:
                    conditionType = ConditionType.MOTION;
                    break;
                case ConditionTypeAvro.LUMINOSITY:
                    conditionType = ConditionType.LUMINOSITY;
                    break;
                case ConditionTypeAvro.SWITCH:
                    conditionType = ConditionType.SWITCH;
                    break;
                case ConditionTypeAvro.TEMPERATURE:
                    conditionType = ConditionType.TEMPERATURE;
                    break;
                case ConditionTypeAvro.CO2LEVEL:
                    conditionType = ConditionType.CO2LEVEL;
                    break;
                case ConditionTypeAvro.HUMIDITY:
                    conditionType = ConditionType.HUMIDITY;
                    break;
                default:
                    throw new IllegalArgumentException("Не найден тип условия для условия: " + scenarioConditionAvro);
            }

            ConditionOperation conditionOperation;
            switch (scenarioConditionAvro.getOperation()) {
                case ConditionOperationAvro.EQUALS:
                    conditionOperation = ConditionOperation.EQUALS;
                    break;
                case ConditionOperationAvro.LOWER_THAN:
                    conditionOperation = ConditionOperation.LOWER_THAN;
                    break;
                case ConditionOperationAvro.GREATER_THAN:
                    conditionOperation = ConditionOperation.GREATER_THAN;
                    break;
                default:
                    throw new IllegalArgumentException("Не найден тип операции для условия: " + scenarioConditionAvro);
            }

            int value = 0;
            if (scenarioConditionAvro.getValue() instanceof Integer) {
                value = (int) scenarioConditionAvro.getValue();
            } else if (scenarioConditionAvro.getValue() instanceof Boolean) {
                if ((boolean) scenarioConditionAvro.getValue()) {
                    value = 1;
                } else {
                    value = 0;
                }
            }

            Condition condition;
            Optional<Condition> optCondition = conditionRepository.findByTypeAndOperationAndValue(conditionType, conditionOperation, value);
            if (optCondition.isPresent()) {
                condition = optCondition.get();
            } else {
                condition = new Condition();
                condition.setType(conditionType);
                condition.setOperation(conditionOperation);
                condition.setValue(value);
                conditionRepository.save(condition);
            }

            conditions.put(scenarioConditionAvro.getSensorId(), condition);
        }

        Map<String, Action> actions = new HashMap<>();
        for (DeviceActionAvro deviceActionAvro : scenarioAddedEventAvro.getActions()) {
            if (!sensorRepository.existsById(deviceActionAvro.getSensorId())) {
                throw new NotFoundException("Не найдено устройство с id: " + deviceActionAvro.getSensorId());
            }
            ActionType actionType;
            switch (deviceActionAvro.getType()) {
                case ActionTypeAvro.ACTIVATE:
                    actionType = ActionType.ACTIVATE;
                    break;
                case ActionTypeAvro.DEACTIVATE:
                    actionType = ActionType.DEACTIVATE;
                    break;
                case ActionTypeAvro.INVERSE:
                    actionType = ActionType.INVERSE;
                    break;
                case ActionTypeAvro.SET_VALUE:
                    actionType = ActionType.SET_VALUE;
                    break;
                default:
                    throw new IllegalArgumentException("Не найден тип действия для действия: " + deviceActionAvro);
            }
            int value = 0;
            if (deviceActionAvro.getValue() instanceof Integer) {
                value = (int) deviceActionAvro.getValue();
            }

            Action action;
            Optional<Action> optAction = actionRepository.findByTypeAndValue(actionType, value);
            if (optAction.isPresent()) {
                action = optAction.get();
            } else {
                action = new Action();
                action.setType(actionType);
                action.setValue(value);
                actionRepository.save(action);
            }

            actions.put(deviceActionAvro.getSensorId(), action);
        }

        scenario.setConditions(conditions);
        scenario.setActions(actions);
        scenarioRepository.save(scenario);
        log.info("Записали новый сценарий {}", scenario);
    }

    private void deleteDevice(DeviceRemovedEventAvro deviceRemovedEventAvro) {
        if (!sensorRepository.existsById(deviceRemovedEventAvro.getId())) {
            sensorRepository.deleteById(deviceRemovedEventAvro.getId());
            log.info("Удалено устройство {}", deviceRemovedEventAvro);
        }
    }

    private void deleteScenario(ScenarioRemovedEventAvro scenarioRemovedEventAvro, String hubId) {
        Optional<Scenario> optScenario = scenarioRepository.findByHubIdAndName(hubId, scenarioRemovedEventAvro.getName());
        if (optScenario.isPresent()) {
            scenarioRepository.delete(optScenario.get());
            log.info("Удален сценарий {}", scenarioRemovedEventAvro);
        }
    }
}
