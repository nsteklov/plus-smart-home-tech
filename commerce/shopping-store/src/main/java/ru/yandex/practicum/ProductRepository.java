package ru.yandex.practicum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.ProductCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByProductCategory(ProductCategory category, Pageable pageable);

    Optional<Product> findByProductId(UUID uuid);

    @Query("SELECT p FROM Product p " +
            "WHERE p.productId in :uuids")
    List<Product> findByProductIds(List<UUID> uuids);
}
