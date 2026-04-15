package ru.yandex.practicum.commerce.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ShoppingCartDto {

    private String shoppingCartId;
    private Map<String, Integer> products;
}
