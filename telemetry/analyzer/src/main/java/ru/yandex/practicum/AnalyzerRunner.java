package ru.yandex.practicum;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.service.HubEventProcessor;
import ru.yandex.practicum.service.SnapshotProcessor;

@Component
public class AnalyzerRunner implements CommandLineRunner {
    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;

    public AnalyzerRunner(HubEventProcessor hubEventProcessor, SnapshotProcessor snapshotProcessor) {
        this.hubEventProcessor = hubEventProcessor;
        this.snapshotProcessor = snapshotProcessor;
    }

    @Override
    public void run(String... args) throws Exception {
        // запускаем в отдельном потоке обработчик событий
        // от пользовательских хабов
        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        // В текущем потоке начинаем обработку
        // снимков состояния датчиков
        snapshotProcessor.start();
    }
}
