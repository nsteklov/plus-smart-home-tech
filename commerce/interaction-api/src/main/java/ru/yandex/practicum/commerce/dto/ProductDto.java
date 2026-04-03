package ru.yandex.practicum.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private String productId;

    @NotBlank(message = "Имя товара не может быть пустым")
    private String productName;

    private String description;

    private String imageSrc;

    @NotBlank(message = "Состояние количестива товара не может быть пустым")
    private String quantityState;

    @NotBlank(message = "Состояние товара не может быть пустым")
    private String productState;

    @NotBlank(message = "Категория товара не может быть пустой")
    private String productCategory;

    @Positive(message = "Цена товара должна быть положительным числом")
    private double price;
}
