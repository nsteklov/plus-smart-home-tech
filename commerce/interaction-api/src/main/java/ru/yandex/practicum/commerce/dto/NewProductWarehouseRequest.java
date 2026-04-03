package ru.yandex.practicum.commerce.dto;

import lombok.Data;

@Data
public class NewProductWarehouseRequest {

    private String productId;
    private boolean fragile;
    private DimensionDto dimension;
    private Double weight;
}
