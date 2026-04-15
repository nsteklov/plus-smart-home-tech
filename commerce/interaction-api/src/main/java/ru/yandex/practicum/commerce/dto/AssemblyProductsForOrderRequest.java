package ru.yandex.practicum.commerce.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AssemblyProductsForOrderRequest {

    private String orderId;
    private Map<String, Integer> products;
}
