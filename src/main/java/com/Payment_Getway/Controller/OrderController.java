package com.Payment_Getway.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Payment_Getway.Model.Order;
import com.Payment_Getway.Service.OrderService;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public String createOrder(
            @ModelAttribute Order order
    ) {

        orderService.createOrder(order);

        return "redirect:/dashboard/orders";
    }
}
