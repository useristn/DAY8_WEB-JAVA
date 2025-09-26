package vn.iostar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.entity.Category;
import vn.iostar.service.CategoryService;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryRestController {

    private final CategoryService categoryService;

    // Lấy từ application.properties
    @Value("${upload.path}")
    private String uploadDir;

    // Get all categories
    @GetMapping
    public List<Category> getAll() {
        return categoryService.findAll();
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create category
    @PostMapping
    public ResponseEntity<Category> create(
            @RequestParam("name") String name,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {

        String fileName = saveFile(imageFile);

        Category category = new Category();
        category.setName(name);
        category.setImage(fileName);

        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    // Update category
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        Optional<Category> opt = categoryService.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Category category = opt.get();
        category.setName(name);

        String fileName = saveFile(imageFile);
        if (fileName != null) {
            category.setImage(fileName);
        }

        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    // Delete category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Search with pagination
    @GetMapping("/search")
    public Page<Category> search(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        if (name != null && !name.isEmpty()) {
            return categoryService.findByNameContainingIgnoreCase(name, pageable);
        } else {
            return categoryService.findAll(pageable);
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        }
        return null;
    }
}
