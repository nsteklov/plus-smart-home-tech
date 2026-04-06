package ru.yandex.practicum.commerce.dto;

import lombok.Data;

@Data
public class BookedProductsDto {

    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;
}
