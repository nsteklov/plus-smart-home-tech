package ru.yandex.practicum.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Repository
public class CustomProductRepositoryImpl implements CustomProductRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public int decreaseProductsBatch(Map<UUID, Integer> decrements) {
        if (decrements == null || decrements.isEmpty()) {
            return 0;
        }

        StringBuilder jpql = new StringBuilder("UPDATE ProductInWarehouse p SET p.quantity = p.quantity - CASE p.id ");

        int index = 0;
        for (UUID id : decrements.keySet()) {
            jpql.append("WHEN :id").append(index).append(" THEN :qty").append(index).append(" ");
            index++;
        }

        jpql.append("END WHERE p.id IN (");
        for (int i = 0; i < index; i++) {
            if (i > 0) jpql.append(", ");
            jpql.append(":id").append(i);
        }
        jpql.append(") AND p.quantity >= CASE p.id ");

        for (int i = 0; i < index; i++) {
            jpql.append("WHEN :id").append(i).append(" THEN :qty").append(i).append(" ");
        }
        jpql.append("END");

        Query query = entityManager.createQuery(jpql.toString());

        int i = 0;
        for (Map.Entry<UUID, Integer> entry : decrements.entrySet()) {
            query.setParameter("id" + i, entry.getKey());
            query.setParameter("qty" + i, entry.getValue());
            i++;
        }

        return query.executeUpdate();
    }

    @Override
    @Transactional
    public int increaseProductsBatch(Map<UUID, Integer> increments) {
        if (increments == null || increments.isEmpty()) {
            return 0;
        }

        StringBuilder jpql = new StringBuilder("UPDATE ProductInWarehouse p SET p.quantity = p.quantity + CASE p.id ");

        int index = 0;
        for (UUID id : increments.keySet()) {
            jpql.append("WHEN :id").append(index).append(" THEN :qty").append(index).append(" ");
            index++;
        }

        jpql.append("END WHERE p.id IN (");
        for (int i = 0; i < index; i++) {
            if (i > 0) jpql.append(", ");
            jpql.append(":id").append(i);
        }
        jpql.append(")");

        Query query = entityManager.createQuery(jpql.toString());

        int i = 0;
        for (Map.Entry<UUID, Integer> entry : increments.entrySet()) {
            query.setParameter("id" + i, entry.getKey());
            query.setParameter("qty" + i, entry.getValue());
            i++;
        }

        return query.executeUpdate();
    }
}
