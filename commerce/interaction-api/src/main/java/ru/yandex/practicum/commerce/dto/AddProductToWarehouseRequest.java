package ru.yandex.practicum.commerce.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AddProductToWarehouseRequest {

    private String productId;
    private  Integer quantity;
}
