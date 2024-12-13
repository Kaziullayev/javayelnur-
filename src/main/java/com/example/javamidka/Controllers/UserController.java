package com.example.javamidka.Controllers;

import com.example.javamidka.Entities.User;
import com.example.javamidka.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class UserController {
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("photo") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String filePath = UPLOAD_DIR + file.getOriginalFilename();
                file.transferTo(new File(filePath));

                User user = new User(filePath);
                userService.saveUser(user);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/register?error";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("photoPath") String photoPath, Model model) {
        // Логика проверки photoPath
        List<User> users = userService.getAllUsers();
        boolean userExists = users.stream().anyMatch(user -> user.getPhotoPath().equals(photoPath));

        if (userExists) {
            model.addAttribute("photoPath", photoPath);
            return "redirect:/main"; // Перенаправление на главную страницу
        } else {
            return "redirect:/login?error"; // Перенаправление на логин с ошибкой
        }
    }

    @GetMapping("/main")
    public String showAllUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "main";
    }

    // Новый обработчик для обслуживания файлов из папки uploads
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().body(resource);
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read the file!", e);
        }
    }
}

