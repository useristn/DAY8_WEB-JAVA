package vn.iostar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin/product")
public class ProductController {
	@GetMapping("/index")
    public String index() {
        return "admin/product/index";
    }
}
