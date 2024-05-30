package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.File;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    @Autowired
    private final CategoryService categoryService;

    @GetMapping("/categories/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "/categories/add-category";
    }

//    @PostMapping("/categories/add")
//    public String addCategory(@Valid Category category, BindingResult result) {
//        if (result.hasErrors()) {
//            return "/categories/add-category";
//        }
//        categoryService.addCategory(category);
//        return "redirect:/categories";
//    }

    @PostMapping("/categories/add")
    public String addCategory(@Valid Category category, BindingResult result, @RequestParam("image") MultipartFile imageFile) {
        if (result.hasErrors()) {
            return "/categories/add-category";
        }

        if (!imageFile.isEmpty()) {
            try {
                String imageName = saveImage(imageFile);
                category.setThumnail(imageName);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the error appropriately
            }
        }

        categoryService.addCategory(category);
        return "redirect:/categories";
    }

    private String saveImage(MultipartFile imageFile) throws IOException {

        // Đường dẫn tuyệt đối tới thư mục lưu trữ hình ảnh
        String uploadDir = System.getProperty("user.dir") + "/category-images/";
        // Đường dẫn tương đối tới thư mục lưu trữ hình ảnh
//        String uploadDir = "category-images/";

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = imageFile.getOriginalFilename();
        String imagePath = uploadDir + originalFilename;
        File dest = new File(imagePath);
        imageFile.transferTo(dest);
        return originalFilename;
    }

//    private String saveImage(MultipartFile imageFile) throws IOException {
//        // Tạo thư mục nếu chưa tồn tại
//        String uploadDir = "category-images/";
//        Path uploadPath = Paths.get(uploadDir);
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
//
//        String originalFilename = imageFile.getOriginalFilename();
//        String imagePath = uploadDir + originalFilename;
//        File dest = new File(imagePath);
//        imageFile.transferTo(dest);
//        return originalFilename;
//    }

//    // Helper method to save the image
//    private String saveImage(MultipartFile imageFile) throws IOException {
//        String uploadDir = "category-images/";
//        String originalFilename = imageFile.getOriginalFilename();
//        String imagePath = uploadDir + originalFilename;
//        File dest = new File(imagePath);
//        imageFile.transferTo(dest);
//        return originalFilename;
//    }



    // Hiển thị danh sách danh mục
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "/categories/categories-list";
    }
    // GET request to show category edit form
    @GetMapping("/categories/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
        model.addAttribute("category", category);
        return "/categories/update-category";
    }

    // POST request to update category
    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable("id") Long id, @Valid Category category, BindingResult result, Model model) {
        if (result.hasErrors()) {
            category.setId(id);
            return "/categories/update-category";
        }

        categoryService.updateCategory(category);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "redirect:/categories";
    }

    // GET request for deleting category
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));

        categoryService.deleteCategoryById(id);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "redirect:/categories";
    }
}
