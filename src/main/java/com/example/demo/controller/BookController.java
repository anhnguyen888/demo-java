package com.example.buoi3lt.controller;

import com.example.buoi3lt.entity.Book;
import com.example.buoi3lt.entity.Cart;
import com.example.buoi3lt.entity.CartItem;
import com.example.buoi3lt.entity.Image;
import com.example.buoi3lt.services.BookService;
import com.example.buoi3lt.services.CartSessionService;
import com.example.buoi3lt.services.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class BookController {
    @Autowired
    private BookService bookService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CartSessionService cartSessionService;

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        Cart cart = cartSessionService.getCart(session);
        model.addAttribute("carts", cart);
        return "cart/cart";
    }
    @PostMapping("/cart/add")
    public String addToCart( HttpSession session, @RequestParam("productId") Integer productId,
                            @RequestParam("productName") String productName
                            ) {
        Cart cart = cartSessionService.getCart(session);
        CartItem item = new CartItem(productId, productName, 1);
        cart.addItem(item);
        cartSessionService.updateCart(session, cart);
        return "redirect:/cart";
    }
    @GetMapping
    public String index(Model model){
        model.addAttribute("books", bookService.getBookList());
        model.addAttribute("images", bookService.getImageList());
        return "home/index";
    }
    @GetMapping("/books")
    public String showBook(Model model){
        model.addAttribute("books", bookService.getBookList());
        model.addAttribute("images", bookService.getImageList());
        return "book/show-book";
    }
    @GetMapping("/add-book")
    public String showFormAddBook(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add-book";
    }

    @PostMapping("/add-book")
    public String processAddBookForm(@Valid @ModelAttribute("book") Book book, BindingResult result, @RequestParam("imageList") MultipartFile[] images, Model model ) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("selected", book.getCategory().getId());
            return "book/add-book";
        } else {
            bookService.addBook(book);
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = saveImage(image);
                        Image bookImage = new Image();
                        if (book.getImages().isEmpty()) {
                            bookImage.setView(true);
                        }
                        bookImage.setImage("/images/" +imageUrl);
                        bookImage.setBook(book);
                        book.getImages().add(bookImage);
                        bookService.addImage(bookImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle image upload error
                    }
                }
            }

            return "redirect:/books";
        }
    }

    private String saveImage(MultipartFile image) throws IOException {
        File saveFile = new ClassPathResource("static/images").getFile();
            String fileName = UUID.randomUUID()+ "." + StringUtils.getFilenameExtension(image.getOriginalFilename());
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
            Files.copy(image.getInputStream(), path);
            return fileName;
    }

    @GetMapping("/edit-book/{id}")
    public String showFormEditBook(@PathVariable int id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        model.addAttribute("selected", book.getCategory().getId());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("images", bookService.getImageByBookId(id));
        return "book/edit-book";
    }
    @PostMapping("/edit-book")
    public String processEditBookForm(@Valid @ModelAttribute("book") Book book, BindingResult result, @RequestParam("imageList") MultipartFile[] images, Model model )throws IOException{
        if (result.hasErrors()) {
            model.addAttribute("selected", book.getCategory().getId());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("images", bookService.getImageByBookId(book.getId()));
            return "book/edit-book";
        } else {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = saveImage(image);

                        // Create book image entity and associate it with the bookl
                        Image bookImage = new Image();
                        bookImage.setImage("/images/" +imageUrl);
                        bookImage.setBook(book);
                        book.getImages().add(bookImage);
                        bookService.addImage(bookImage);
                        book.setImages(bookService.getBookById(book.getId()).getImages());
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle image upload error
                    }
                }
                else {
                    book.setImages(bookService.getBookById(book.getId()).getImages());
                }
            }
            bookService.saveBook(book);
            return "redirect:/books";
        }
    }
    @GetMapping("/set-view-image/{id}")
    public String setViewImage(@PathVariable int id, Model model) {
        //set view image = false all
        List<Image> images = bookService.getImageByBookId(bookService.getImageById(id).getBook().getId());
        for (Image image : images) {
            image.setView(false);
            bookService.addImage(image);
        }
        Image image = bookService.getImageById(id);
        image.setView(true);
        bookService.addImage(image);
        return "redirect:/edit-book/"+image.getBook().getId();
    }
    @GetMapping("/delete-image/{id}")
    public String deleteImage(@PathVariable int id, Model model) {
        Image image = bookService.getImageById(id);
        bookService.removeImage(id);
        return "redirect:/edit-book/"+image.getBook().getId();
    }
    @GetMapping("/delete-book/{id}")
    public String deleteBook(@PathVariable int id, Model model) {
        bookService.removeBook(id);
        return "redirect:/books";
    }
    @GetMapping("/categories/show/{id}")
    public String showBookByCategory(@PathVariable int id, Model model){
        model.addAttribute("books", bookService.getBookByCategoryId(id));
        model.addAttribute("images", bookService.getImageList());
        return "book/show-book";
    }
    @GetMapping("/search")
    public String searchBook(@RequestParam("search") String search, Model model){
        model.addAttribute("books", bookService.searchBook(search));
        model.addAttribute("images", bookService.getImageList());
        return "book/show-book";
    }
}
