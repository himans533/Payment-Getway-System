package com.Payment_Getway.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Payment_Getway.Service.ProductService;
import org.springframework.ui.Model;
@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String getAllProducts(
            Model model
    ) {

        return "redirect:/dashboard/products";
    }

    @GetMapping("/{id}")
    public String getProductDetails(
            @PathVariable Long id,
            Model model
    ) {

        return "redirect:/dashboard/products";
    }
}
