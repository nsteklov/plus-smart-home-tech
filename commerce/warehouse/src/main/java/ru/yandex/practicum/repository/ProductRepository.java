package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.ProductInWarehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends
        JpaRepository<ProductInWarehouse, UUID>,
        CustomProductRepository {

    Optional<ProductInWarehouse> findByProductId(UUID uuid);

    boolean existsByProductId(UUID productId);

    @Query("SELECT p FROM ProductInWarehouse p WHERE p.productId IN :productIds")
    List<ProductInWarehouse> findByProductIds(@Param("productIds") List<UUID> productIds);
}
