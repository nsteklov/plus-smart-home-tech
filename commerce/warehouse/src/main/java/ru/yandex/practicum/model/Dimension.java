package ru.yandex.practicum.model;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Dimension {

    @Min(value = 1, message = "Ширина товара должна быть больше 1")
    private Double width;

    @Min(value = 1, message = "Высота товара должна быть больше 1")
    private Double height;

    @Min(value = 1, message = "Глубина товара должна быть больше 1")
    private Double depth;
}
