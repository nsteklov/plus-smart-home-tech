package ru.yandex.practicum.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SetProductQuantityStateRequest {

    private String productId;

    @NotBlank(message = "Состояние количестива товара не может быть пустым")
    private String quantityState;
}
