package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaPropertiesConfigAnalyzer;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class SnapshotProcessor {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaPropertiesConfigAnalyzer propertiesConfig;
    private Consumer<String, SensorsSnapshotAvro> consumer;
    private String snapshotTopic;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public SnapshotProcessor(KafkaPropertiesConfigAnalyzer propertiesConfig, SensorRepository sensorRepository, ScenarioRepository scenarioRepository, ConditionRepository conditionRepository, ActionRepository actionRepository) {
        this.propertiesConfig = propertiesConfig;
        this.scenarioRepository = scenarioRepository;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;

        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, propertiesConfig.getClientIdSnapshot());
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, propertiesConfig.getGroupIdSnapshot());
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, propertiesConfig.getBootstrapServers());
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, propertiesConfig.getKeyDeserializer());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, propertiesConfig.getSnapshotValueDeserializer());
        consumerConfig.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, propertiesConfig.getMaxPollRecordsConfig());
        consumerConfig.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, propertiesConfig.getFetchMaxBytesConfig());
        consumerConfig.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, propertiesConfig.getMaxPartitionFetchBytesConfig());
        consumer = new KafkaConsumer<>(consumerConfig);

        snapshotTopic = propertiesConfig.getSnapshotTopic();
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            // подписываемся на топики
            List<String> topicsConsumer = new ArrayList<>();
            topicsConsumer.add(snapshotTopic);
            consumer.subscribe(topicsConsumer);

            // начинаем Poll Loop
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                int count = 0;
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
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
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record, int count, Consumer<String, SensorsSnapshotAvro> consumer) {
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

    private void handleRecord(ConsumerRecord<String, SensorsSnapshotAvro> record) throws InterruptedException {
        log.info("Принимаем сообщение со снэпшотом, топик = {}, партиция = {}, смещение = {}, значение: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());
        SensorsSnapshotAvro event = record.value();
        Map<String, SensorStateAvro> sensorsState = event.getSensorsState();
        String hubId = event.getHubId();
        List<Scenario> scenarios = scenarioRepository.findByHubIdWithConditionsAndActions(hubId);
        System.out.println(scenarios);
        Condition condition;
        Action action;
        for (Scenario scenario : scenarios) {
            for (Map.Entry<String, SensorStateAvro> entry : sensorsState.entrySet()) {
                Optional<Condition> optCondition = scenario.getConditions().entrySet().stream()
                        .filter(curCondition -> curCondition.getKey().equals(entry.getKey()))
                        .map(curCondition -> curCondition.getValue())
                        .findFirst();
                if (!optCondition.isPresent()) {
                    continue;
                }
                condition = optCondition.get();
                log.info("Получили условие " + condition);
                Object sensorAvro = entry.getValue().getData();
                log.info("Получили данные датчика " + sensorAvro);
                if (sensorAvro instanceof ClimateSensorAvro
                        && (condition.getType() == ConditionType.TEMPERATURE && conditionMet(condition.getOperation(), ((ClimateSensorAvro) sensorAvro).getTemperatureC(), condition.getValue())
                            || condition.getType() == ConditionType.HUMIDITY && conditionMet(condition.getOperation(), ((ClimateSensorAvro) sensorAvro).getHumidity(), condition.getValue())
                            || condition.getType() == ConditionType.CO2LEVEL &&  conditionMet(condition.getOperation(), ((ClimateSensorAvro) sensorAvro).getCo2Level(), condition.getValue()))
                    || sensorAvro instanceof LightSensorAvro
                        && condition.getType() == ConditionType.LUMINOSITY && conditionMet(condition.getOperation(), ((LightSensorAvro) sensorAvro).getLuminosity(), condition.getValue())
                    || sensorAvro instanceof MotionSensorAvro
                        && (condition.getType() == ConditionType.MOTION && ((MotionSensorAvro) sensorAvro).getMotion() == true && conditionMet(condition.getOperation(), 1, condition.getValue())
                            || condition.getType() == ConditionType.MOTION && ((MotionSensorAvro) sensorAvro).getMotion() == false && conditionMet(condition.getOperation(), 0, condition.getValue()))
                    || sensorAvro instanceof SwitchSensorAvro
                        && (condition.getType() == ConditionType.SWITCH && ((SwitchSensorAvro) sensorAvro).getState() == true && conditionMet(condition.getOperation(), 1, condition.getValue())
                            || condition.getType() == ConditionType.SWITCH && ((SwitchSensorAvro) sensorAvro).getState() == false && conditionMet(condition.getOperation(), 0, condition.getValue()))
                    || sensorAvro instanceof TemperatureSensorAvro
                        && condition.getType() == ConditionType.TEMPERATURE && conditionMet(condition.getOperation(), ((TemperatureSensorAvro) sensorAvro).getTemperatureC(), condition.getValue())) {
                    Optional<Action> optAction = scenario.getActions().entrySet().stream()
                            .filter(curAction -> curAction.getKey().equals(entry.getKey()))
                            .map(curAction -> curAction.getValue())
                            .findFirst();

                    if (optAction.isPresent()) {
                        action =  optAction.get();
                        log.info("Получили действие " + action);
                        ActionTypeProto actionTypeProto;
                        switch (action.getType()) {
                            case ACTIVATE:
                                actionTypeProto = ActionTypeProto.ACTIVATE;
                                break;
                            case DEACTIVATE:
                                actionTypeProto = ActionTypeProto.DEACTIVATE;
                                break;
                            case INVERSE:
                                actionTypeProto = ActionTypeProto.INVERSE;
                                break;
                            case SET_VALUE:
                                actionTypeProto = ActionTypeProto.SET_VALUE;
                                break;
                            default:
                                throw new IllegalArgumentException("Не найден тип действия для условия: " + condition);
                        }
                        DeviceActionRequest deviceActionRequest = DeviceActionRequest.newBuilder()
                                .setHubId(hubId)
                                .setScenarioName(scenario.getName())
                                .setAction(
                                        DeviceActionProto.newBuilder()
                                                .setSensorId(entry.getKey())
                                                .setType(actionTypeProto)
                                                .setValue(action.getValue())
                                                .build()
                                )
                                .setTimestamp(Timestamp.newBuilder()
                                        .setSeconds(Instant.now().getEpochSecond())
                                        .setNanos(Instant.now().getNano())
                                        .build()
                                )
                                .build();
                        log.info("Отправляю данные о действии: {} от условия: {}", deviceActionRequest, condition);
                        try {
                            hubRouterClient.handleDeviceAction(deviceActionRequest);
                        } catch (Exception e) {
                            log.error("Возникла ошибка при отправке в hub-router", e);
                        }
                    }
                }
            }
        }
    }

    public boolean conditionMet(ConditionOperation  conditionOperation, int value1, int value2) {
        if (conditionOperation == ConditionOperation.EQUALS && value1 == value2
            || conditionOperation == ConditionOperation.GREATER_THAN && value1 > value2
            || conditionOperation == ConditionOperation.LOWER_THAN) {
            return true;
        } else {
            return false;
        }
    }
}
