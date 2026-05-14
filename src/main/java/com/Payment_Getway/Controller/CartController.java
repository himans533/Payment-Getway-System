package com.Payment_Getway.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Payment_Getway.Service.CartService;
import com.Payment_Getway.Service.UserService;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public String addToCart(
            Principal principal,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity
    ) {

        cartService.addToCart(
                userService.getRequiredUserByEmail(principal.getName()),
                productId,
                quantity
        );

        return "redirect:/dashboard/cart";
    }

    @PostMapping("/update")
    public String updateCart(
            Principal principal,
            @RequestParam Long cartId,
            @RequestParam Integer quantity
    ) {

        cartService.updateQuantity(
                userService.getRequiredUserByEmail(principal.getName()),
                cartId,
                quantity
        );

        return "redirect:/dashboard/cart";
    }

    @PostMapping("/remove")
    public String removeCartItem(
            Principal principal,
            @RequestParam Long cartId
    ) {

        cartService.removeItem(
                userService.getRequiredUserByEmail(principal.getName()),
                cartId
        );

        return "redirect:/dashboard/cart";
    }
}
