package vn.iostar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    
    @GetMapping("/")
    public String redirectToAdmin() {
        return "redirect:/admin/index";
    }
    
    @GetMapping("/admin/index")
    public String adminIndex() {
        return "admin/index";
    }
}
