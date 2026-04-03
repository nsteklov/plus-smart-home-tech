package ru.yandex.practicum.commerce.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ChangeProductQuantityRequest {

    private String productId;

    @Min(value = 1, message = "Количество должно быть больше 1")
    Integer newQuantity;
}
