package ru.yandex.practicum.commerce.dto;

import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class ShoppingCartDto {

    private UUID shoppingCartId;

    private Map<UUID, Integer> products;
}
