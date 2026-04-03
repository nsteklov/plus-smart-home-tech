package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.dto.ProductDto;
import ru.yandex.practicum.commerce.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.model.QuantityState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        log.info("Создание товара с названием : {}", productDto.getProductName());

        Product product = ProductMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(product);
        log.info("Товар создан с UUID: {}", savedProduct.getProductId());

        return ProductMapper.toDto(savedProduct);
    }

    public Page<ProductDto> getProductsByCategory(String categoryString, int page, int size, String[] sortArray) {

        List<Sort.Order> orders = new ArrayList<>();
        for (String s : sortArray) {
            String[] sortElements = s.split(",");
            if (sortElements.length == 2) {
                if (sortElements[1].equals("desc")) {
                    orders.add(new Sort.Order(Sort.Direction.DESC, sortElements[0]));
                } else {
                    orders.add(new Sort.Order(Sort.Direction.ASC, sortElements[0]));
                }
            } else if (sortElements.length == 1) {
                orders.add(new Sort.Order(Sort.Direction.ASC, sortElements[0]));
            }
        }
        Sort sort = Sort.by(orders);
        Pageable pageable = PageRequest.of(page, size, sort);
        ProductCategory category;
        try {
            category = ProductCategory.valueOf(categoryString);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Категория товара " + categoryString + " не найдена");
        }
        Page<Product> productPage = productRepository.findByProductCategory(category, pageable);
        log.info("Получен список товаров с категорией: {}", category);
        return productPage.map(product -> ProductMapper.toDto(product));
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {

        UUID uuid;
        try {
            uuid = UUID.fromString(productDto.getProductId());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID " + productDto.getProductId());
        }

        Product product = productRepository.findByProductId(uuid)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден", HttpStatus.NOT_FOUND, "Товар с UUID " + productDto.getProductId() + " не найден"));

        if (productDto.getProductName() != null) {
            product.setProductName(productDto.getProductName());
        }
        if (productDto.getDescription() != null) {
            product.setDescription(productDto.getDescription());
        }
        if (productDto.getImageSrc() != null) {
            product.setImageSrc(productDto.getImageSrc());
        }
        if (productDto.getImageSrc() != null) {
            product.setImageSrc(productDto.getImageSrc());
        }
        if (productDto.getQuantityState() != null) {
            try {
                QuantityState quantityState = QuantityState.valueOf(productDto.getQuantityState());
                product.setQuantityState(quantityState);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Состояние количества товара " + productDto.getQuantityState() + " не найдено");
            }
        }
        if (productDto.getProductState() != null) {
            try {
                ProductState productState = ProductState.valueOf(productDto.getProductState());
                product.setProductState(productState);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Состояние товара " + productDto.getProductState() + " не найдено");
            }
        }
        if (productDto.getProductCategory() != null) {
            try {
                ProductCategory category = ProductCategory.valueOf(productDto.getProductCategory());
                product.setProductCategory(category);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Категория товара " + productDto.getProductCategory() + " не найдена");
            }
        }
        if (productDto.getPrice() > 0) {
            product.setPrice(productDto.getPrice());
        }
        Product updatedProduct = productRepository.save(product);
        log.info("Товар с UUID {} обновлен", productDto.getProductId());

        return ProductMapper.toDto(updatedProduct);
    }

    @Transactional
    public Boolean removeProduct(String productId) {

        UUID uuid;
        try {
            uuid = UUID.fromString(productId);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID " + productId);
        }

        Product product = productRepository.findByProductId(uuid)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден", HttpStatus.NOT_FOUND, "Товар с UUID " + productId + " не найден"));

        productRepository.delete(product);
        log.info("Товар с UUID {} удален", productId);
        return true;
    }

    @Transactional
    public Boolean updateQuantityState(SetProductQuantityStateRequest setProductQuantityStateRequest) {

        UUID uuid;
        try {
            uuid = UUID.fromString(setProductQuantityStateRequest.getProductId());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID " + setProductQuantityStateRequest.getProductId());
        }

        Product product = productRepository.findByProductId(uuid)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден", HttpStatus.NOT_FOUND, "Товар с UUID " + setProductQuantityStateRequest.getProductId() + " не найден"));

        if (setProductQuantityStateRequest.getQuantityState() != null) {
            try {
                QuantityState quantityState = QuantityState.valueOf(setProductQuantityStateRequest.getQuantityState());
                product.setQuantityState(quantityState);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Состояние количества товара " + setProductQuantityStateRequest.getQuantityState() + " не найдено");
            }
        }

        return true;
    }

    public ProductDto getProductById(String productId) {

        UUID uuid;
        try {
            uuid = UUID.fromString(productId);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Передан некорректный формат UUID " + productId);
        }

        Product product = productRepository.findByProductId(uuid)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден", HttpStatus.NOT_FOUND, "Товар с UUID " + productId + " не найден"));

        return ProductMapper.toDto(product);
    }
}
