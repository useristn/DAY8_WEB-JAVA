package vn.iostar.controller;

import vn.iostar.entity.Category;
import vn.iostar.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Value("${upload.path}")
    private String uploadDir; // lấy từ application.properties

    @GetMapping("/index")
    public String index() {
        return "admin/categories/index";
    }
    
    @GetMapping("/add")
    public String add(ModelMap model) {
        model.addAttribute("category", new Category());
        return "admin/categories/addOrEdit";
    }

    @PostMapping("/saveOrUpdate")
    public ModelAndView saveOrUpdate(
            ModelMap model,
            @Valid @ModelAttribute("category") Category cateModel,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return new ModelAndView("admin/categories/addOrEdit");
        }

        Category entity = new Category();
        BeanUtils.copyProperties(cateModel, entity);

        try {
            if (file != null && !file.isEmpty()) {
                // Tạo thư mục nếu chưa tồn tại
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Tên file tránh trùng
                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path target = uploadPath.resolve(filename);

                // Lưu file ra ổ đĩa
                file.transferTo(target.toFile());

                // Lưu URL vào entity
                entity.setImage("/uploads/" + filename);
            } else {
                // Nếu không upload ảnh mới → giữ lại ảnh cũ trong DB
                if (entity.getId() != null) {
                    categoryService.findById(entity.getId())
                            .ifPresent(old -> entity.setImage(old.getImage()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "Upload file failed!");
            return new ModelAndView("admin/categories/addOrEdit", model);
        }

        // Lưu vào DB
        categoryService.createCategory(entity);

        String message = (entity.getId() != null) ? "Category is updated!" : "Category is saved!";
        redirectAttributes.addFlashAttribute("message", message);

        return new ModelAndView("redirect:/admin/categories/searchpaginated");
    }


    @GetMapping("/list")
    public String list(ModelMap model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(ModelMap model, @PathVariable("id") Long id, RedirectAttributes ra) {
        Optional<Category> opt = categoryService.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("category", opt.get());
            return new ModelAndView("admin/categories/addOrEdit", model);
        }
        ra.addFlashAttribute("message", "Category is not existed!!!");
        return new ModelAndView("redirect:/admin/categories/list");
    }

    @GetMapping("/delete/{id}")
    public ModelAndView delete(ModelMap model, @PathVariable("id") Long id, RedirectAttributes ra) {
        categoryService.deleteCategory(id);
        ra.addFlashAttribute("message", "Category is deleted!!!");
        return new ModelAndView("redirect:/admin/categories/searchpaginated");
    }

    @GetMapping("/searchpaginated")
    public String search(
            ModelMap model,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        int currentPage = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 5 : size;

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by("name"));
        Page<Category> resultPage;

        if (name != null && !name.isEmpty()) {
            resultPage = categoryService.findByNameContainingIgnoreCase(name, pageable);
            model.addAttribute("name", name);
        } else {
            resultPage = categoryService.findAll(pageable);
        }

        int totalPages = resultPage.getTotalPages();
        if (totalPages > 0) {
            int start = Math.max(1, currentPage - 2);
            int end = Math.min(currentPage + 2, totalPages);

            if (totalPages > 5) {
                if (end == totalPages) start = end - 4; // show 5 pages
                else if (start == 1) end = start + 4;
            }

            List<Integer> pageNumbers = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("categoryPage", resultPage);
        return "admin/categories/searchpaginated";
    }
}
