package vn.iostar.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.entity.Product;
import vn.iostar.entity.Category;
import vn.iostar.service.ProductService;
import vn.iostar.service.CategoryService;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Value("${upload.path}")
    private String uploadDir;

    // Get all products
    @GetMapping
    public Page<Product> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productService.getAllProducts(pageable); // cần service trả Page<Product>
    }

    // Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // Create product
    @PostMapping
    public ResponseEntity<Product> create(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        Optional<Category> categoryOpt = categoryService.findById(categoryId);
        if (categoryOpt.isEmpty()) return ResponseEntity.badRequest().build();

        String fileName = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName),
                       StandardCopyOption.REPLACE_EXISTING);
        }

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setCategory(categoryOpt.get());
        product.setImage(fileName);

        return ResponseEntity.ok(productService.saveProduct(product));
    }

    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        Optional<Product> opt = productService.getProductById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<Category> categoryOpt = categoryService.findById(categoryId);
        if (categoryOpt.isEmpty()) return ResponseEntity.badRequest().build();

        Product product = opt.get();
        product.setName(name);
        product.setPrice(price);
        product.setCategory(categoryOpt.get());

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName),
                       StandardCopyOption.REPLACE_EXISTING);
            product.setImage(fileName);
        }

        return ResponseEntity.ok(productService.saveProduct(product));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
