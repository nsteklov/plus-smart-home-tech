package ru.yandex.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "products", schema = "public")
@Data
@NoArgsConstructor
public class ProductInWarehouse {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "is_fragile")
    private boolean fragile;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "width", column = @Column(name = "width", nullable = false)),
            @AttributeOverride(name = "height", column = @Column(name = "height", nullable = false)),
            @AttributeOverride(name = "depth", column = @Column(name = "depth", nullable = false))
    })
    private Dimension dimension;

    @Min(value = 1, message = "Вес товара должен быть больше 1")
    @Column(nullable = false)
    private Double weight;

    @Column
    private Integer quantity;
}
