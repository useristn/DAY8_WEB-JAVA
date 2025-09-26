package vn.iostar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.iostar.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
