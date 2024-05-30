package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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
import java.util.UUID;

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
                String imageName = saveImageStatic(imageFile);
                category.setThumnail("/images/" +imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        categoryService.addCategory(category);
        return "redirect:/categories";
    }

    private String saveImageStatic(MultipartFile image) throws IOException {
        File saveFile = new ClassPathResource("static/images").getFile();
        String fileName = UUID.randomUUID()+ "." + StringUtils.getFilenameExtension(image.getOriginalFilename());
        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
        Files.copy(image.getInputStream(), path);
        return fileName;
    }

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
