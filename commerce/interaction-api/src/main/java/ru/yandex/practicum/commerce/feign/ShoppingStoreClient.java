package ru.yandex.practicum.commerce.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.ProductDto;

import java.util.List;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient {

    @PostMapping("/api/v1/shopping-store/products")
    List<ProductDto> getProductsByIds(@RequestBody String[] productIds);
}
