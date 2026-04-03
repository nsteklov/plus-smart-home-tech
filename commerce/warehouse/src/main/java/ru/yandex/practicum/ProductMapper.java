package ru.yandex.practicum;

import ru.yandex.practicum.commerce.dto.DimensionDto;
import ru.yandex.practicum.commerce.dto.NewProductWarehouseRequest;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.ProductInWarehouse;

import java.util.UUID;

public class ProductMapper {

    public static ProductInWarehouse toEntity(NewProductWarehouseRequest newProductWarehouseRequest, UUID uuid) {
        ProductInWarehouse product = new ProductInWarehouse();
        product.setProductId(uuid);
        product.setFragile(newProductWarehouseRequest.isFragile());
        if (newProductWarehouseRequest.getDimension() != null) {
            DimensionDto dimensionDto = newProductWarehouseRequest.getDimension();
            Dimension dimension = new Dimension();
            dimension.setWidth(dimensionDto.getWidth());
            dimension.setHeight(dimensionDto.getHeight());
            dimension.setDepth(dimensionDto.getDepth());
            product.setDimension(dimension);
        }
        product.setWeight(newProductWarehouseRequest.getWeight());
        return product;
    }

}