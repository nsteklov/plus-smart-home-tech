package ru.yandex.practicum.commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseAddressDto {

    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
    private String name;
}
