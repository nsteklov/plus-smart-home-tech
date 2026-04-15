package ru.yandex.practicum.commerce.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public class ProductReturnRequest {

    private String orderId;
    private Map<String, Integer> products;
}
