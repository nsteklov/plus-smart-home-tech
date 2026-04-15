package ru.yandex.practicum.commerce.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.dto.*;

import java.util.Map;

@FeignClient(name = "warehouse")
public interface WarehouseClient {

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductsInWarehouse(@RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assembly(@RequestBody AssemblyProductsForOrderRequest assemblyProductsForOrderRequest);

    @PostMapping("/api/v1/warehouse/return")
    boolean returnProducts(@RequestBody Map<String, Integer> products);

    @GetMapping("/api/v1/warehouse/address")
    WarehouseAddressDto getWarehouseAddress();

    @PostMapping("/api/v1/warehouse/shipped")
    void shipToDelivery(@RequestBody ShippedToDeliveryRequest shippedToDeliveryRequest);
}
