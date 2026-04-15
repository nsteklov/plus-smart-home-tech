package ru.yandex.practicum.repository;

import java.util.Map;
import java.util.UUID;

public interface CustomProductRepository {
    int decreaseProductsBatch(Map<UUID, Integer> decrements);
    int increaseProductsBatch(Map<UUID, Integer> increments);
}
