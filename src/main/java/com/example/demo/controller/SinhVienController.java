package com.example.demo.controller;

import com.example.demo.model.SinhVien;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SinhVienController {
    @GetMapping("/sinhvien")
    public String showForm(Model model) {
        model.addAttribute("sinhVien", new SinhVien());
        return "sinhvien/form-sinhvien";
    }
    @PostMapping("/sinhvien")
    public String submitForm(@Valid SinhVien sinhVien, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "sinhvien/form-sinhvien";
        }
        model.addAttribute("message", "Sinh viên đã được thêm thành công!");
        return "sinhvien/result-sinhvien";
    }
}
