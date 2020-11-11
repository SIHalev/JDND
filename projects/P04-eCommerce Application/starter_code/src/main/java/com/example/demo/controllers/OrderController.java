package com.example.demo.controllers;

import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

  private UserRepository userRepository;
  private OrderRepository orderRepository;

  @Autowired
  public OrderController(
      UserRepository userRepository,
      OrderRepository orderRepository) {
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
  }

  @PostMapping("/submit/{username}")
  public ResponseEntity<UserOrder> submit(@PathVariable String username) {
    LOGGER.debug("Trying to sumbit order for {}", username);
    User user = userRepository.findByUsername(username);
    if (user == null) {
      LOGGER.info("Order submit failed, unknown user");
      return ResponseEntity.notFound().build();
    }
    UserOrder order = UserOrder.createFromCart(user.getCart());
    orderRepository.save(order);
    LOGGER.debug("Order submit for user {} was successful", username);
    return ResponseEntity.ok(order);
  }

  @GetMapping("/history/{username}")
  public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
    User user = userRepository.findByUsername(username);
    if (user == null) {
      LOGGER.info("Unknown user {}", username);
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(orderRepository.findByUser(user));
  }
}
