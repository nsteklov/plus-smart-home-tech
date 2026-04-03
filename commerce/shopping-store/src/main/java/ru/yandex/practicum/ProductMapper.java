package ru.yandex.practicum;

import ru.yandex.practicum.commerce.dto.ProductDto;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.model.QuantityState;

public class ProductMapper {

    public static Product toEntity(ProductDto productDto) {
        Product product = new Product();
        product.setProductName(productDto.getProductName());
        product.setDescription(productDto.getDescription());
        product.setImageSrc(productDto.getImageSrc());
        if (productDto.getQuantityState() != null && !productDto.getQuantityState().isBlank()) {
            try {
                product.setQuantityState(QuantityState.valueOf(productDto.getQuantityState()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Состояние количесива  товара " + productDto.getQuantityState() + " не найдено");
            }
        }
        if (productDto.getProductState() != null && !productDto.getProductState().isBlank()) {
            try {
                product.setProductState(ProductState.valueOf(productDto.getProductState()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Состояние товара " + productDto.getProductState() + " не найдено");
            }
        }
        if (productDto.getProductCategory() != null && !productDto.getProductCategory().isBlank()) {
            try {
                product.setProductCategory(ProductCategory.valueOf(productDto.getProductCategory()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Категория товара " + productDto.getProductCategory() + " не найдена");
            }
        }
        product.setPrice(productDto.getPrice());

        return product;
    }

    public static ProductDto toDto(Product product) {
        return ProductDto.builder()
                .productId(product.getProductId().toString())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageSrc(product.getImageSrc())
                .quantityState(product.getQuantityState() != null ? product.getQuantityState().toString() : null)
                .productState(product.getProductState() != null ? product.getProductState().toString() : null)
                .productCategory(product.getProductCategory() != null ? product.getProductCategory().toString() : null)
                .price(product.getPrice())
                .build();
    }
}
