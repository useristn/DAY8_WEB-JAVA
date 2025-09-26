package vn.iostar.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.entity.Category;
import org.springframework.data.domain.Pageable;
import java.util.List;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContainingIgnoreCase(String name);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
