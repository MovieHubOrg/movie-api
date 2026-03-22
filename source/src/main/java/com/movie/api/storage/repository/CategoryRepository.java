package com.movie.api.storage.repository;

import com.movie.api.storage.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> findByIdAndStatus(Long id, Integer status);

    boolean existsByName(String name);
}
