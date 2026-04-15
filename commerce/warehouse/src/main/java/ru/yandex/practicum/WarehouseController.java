package ru.yandex.practicum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/warehouse")
@RequiredArgsConstructor
@Slf4j
@Validated
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public boolean createProduct(@Valid @RequestBody NewProductWarehouseRequest newProductWarehouseRequest) {
        log.info("PUT запрос на добавление товара: {} на склад", newProductWarehouseRequest);
        return warehouseService.createProduct(newProductWarehouseRequest);
    }

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public BookedProductsDto checkProductsInWarehouse(@RequestBody ShoppingCartDto shoppingCartDto) {
        log.info("Проверка наличия товаров на складе в корзине {} ", shoppingCartDto);
        return warehouseService.checkProductsInWarehouse(shoppingCartDto);
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public boolean addProduct(@RequestBody AddProductToWarehouseRequest addProductToWarehouseRequest) {
        log.info("Принять на склад товар {} ", addProductToWarehouseRequest);
        return warehouseService.addProduct(addProductToWarehouseRequest);
    }

    @GetMapping("/address")
    @ResponseStatus(HttpStatus.OK)
    public WarehouseAddressDto getWarehouseAddress() {
        log.info("Получить адрес склада");
        return warehouseService.getWarehouseAddress();
    }

    @PostMapping("/assembly")
    @ResponseStatus(HttpStatus.OK)
    public BookedProductsDto assembly(@RequestBody AssemblyProductsForOrderRequest assemblyProductsForOrderRequest) {
        log.info("Сборка товаров для заказа {} ", assemblyProductsForOrderRequest);
        return warehouseService.assembly(assemblyProductsForOrderRequest);
    }

    @PostMapping("/return")
    @ResponseStatus(HttpStatus.OK)
    public boolean returnProducts(@RequestBody Map<String, Integer> products) {
        log.info("Возврат товаров на склад {} ", products);
        return warehouseService.returnProducts(products);
    }

    @PostMapping("/shipped")
    @ResponseStatus(HttpStatus.OK)
    public void shipToDelivery(@RequestBody ShippedToDeliveryRequest shippedToDeliveryRequest) {
        log.info("Передача товаров в службу доставки {} ", shippedToDeliveryRequest);
        warehouseService.shipToDelivery(shippedToDeliveryRequest);
    }
}
