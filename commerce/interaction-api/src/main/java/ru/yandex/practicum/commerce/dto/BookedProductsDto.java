package ru.yandex.practicum.commerce.dto;

import lombok.Data;

@Data
public class BookedProductsDto {

    private Double deliveryWeight;
    private Double deliveryVolume;
    private boolean fragile;
}
