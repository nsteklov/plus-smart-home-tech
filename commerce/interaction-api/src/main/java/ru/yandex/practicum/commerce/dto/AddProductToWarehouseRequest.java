package ru.yandex.practicum.commerce.dto;

import lombok.Getter;

@Getter
public class AddProductToWarehouseRequest {

    private String productId;

    private  Integer quantity;
}
