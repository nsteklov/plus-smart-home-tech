package ru.yandex.practicum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.ProductDto;
import ru.yandex.practicum.commerce.dto.SetProductQuantityStateRequest;

@RestController
@RequestMapping(path = "/api/v1/shopping-store")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProductController {

    private final ProductService productService;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ProductDto createProduct(@Valid @RequestBody ProductDto productDto) {
        log.info("PUT запрос на создание товара: {}", productDto);
        return productService.createProduct(productDto);
    }

    @GetMapping
    public Page<ProductDto> getProducts(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam (required = false) String[] sort) {

        log.info("GET запрос на получение товаров по категории: {}", category);
        return productService.getProductsByCategory(category, page, size, sort);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        log.info("POST запрос на обновление товара: {}", productDto);
        return productService.updateProduct(productDto);
    }

    @PostMapping("/removeProductFromStore")
    @ResponseStatus(HttpStatus.OK)
    public Boolean removeProductFromStore(@RequestBody String productId) {
        log.info("POST запрос на удаление товара с UUID: {}", productId);
        return productService.removeProduct(productId);
    }

    @PostMapping("/quantityState")
    @ResponseStatus(HttpStatus.OK)
    public Boolean updateQuantityState(@RequestBody SetProductQuantityStateRequest setProductQuantityStateRequest) {
        log.info("POST запрос на изменение стаутса количества товара: {}", setProductQuantityStateRequest);
        return productService.updateQuantityState(setProductQuantityStateRequest);
    }

    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable String productId) {
        log.info("GET запрос на получение товара с UUID: {}", productId);
        return productService.getProductById(productId);
    }
}
