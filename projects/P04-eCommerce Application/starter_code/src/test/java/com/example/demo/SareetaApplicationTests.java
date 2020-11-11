package com.example.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SareetaApplicationTests {

  private CartController cartController;
  private ItemController itemController;
  private OrderController orderController;
  private UserController userController;

  private CartRepository cartRepository = mock(CartRepository.class);
  private ItemRepository itemRepository = mock(ItemRepository.class);
  private OrderRepository orderRepository = mock(OrderRepository.class);
  private UserRepository userRepository = mock(UserRepository.class);
  private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

  @Before
  public void setUp() {
    this.cartController = new CartController(userRepository, cartRepository, itemRepository);
    this.itemController = new ItemController(itemRepository);
    this.orderController = new OrderController(userRepository, orderRepository);
    this.userController = new UserController(encoder, userRepository, cartRepository);
  }

  @Test
  public void findingItemIsWorking() {
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));

    when(itemRepository.findAll()).thenReturn(items);

    final ResponseEntity<List<Item>> response = itemController.getItems();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(items.size(), Objects.requireNonNull(response.getBody()).size());
  }

  @Test
  public void findingItemByIdIsWorking() {
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));

    when(itemRepository.findById(0L)).thenReturn(Optional.of(items.get(0)));

    final ResponseEntity<Item> response = itemController.getItemById(0L);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(items.get(0), response.getBody());
  }

  @Test
  public void addingUnknownItemReturns404() {
    User user = new User(0, "username", "password", new Cart());
    ModifyCartRequest request = new ModifyCartRequest();
    request.setUsername("username");
    request.setItemId(0L);
    request.setQuantity(1);

    when(userRepository.findByUsername("username")).thenReturn(user);
    assertEquals(HttpStatus.NOT_FOUND, cartController.addToCart(request).getStatusCode());
  }

  @Test
  public void addingItemToUnknownUserReturns404() {
    ModifyCartRequest req = new ModifyCartRequest("username", 0, 1);
    Item item = new Item();
    item.setId(0L);
    item.setName("Hades");
    item.setDescription("Great game");
    item.setPrice(BigDecimal.valueOf(19.99));

    when(userRepository.findByUsername("test")).thenReturn(null);
    when(itemRepository.findById(0L)).thenReturn(Optional.of(item));

    assertEquals(HttpStatus.NOT_FOUND, cartController.addToCart(req).getStatusCode());
  }

  @Test
  public void addToCartIsWorking() {
    ModifyCartRequest req = new ModifyCartRequest("username", 0, 1);
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));
    int itemsCount = items.size();
    User user = new User(0, "username", "password");
    Cart cart = new Cart(0L, user, items, BigDecimal.valueOf(0));
    user.setCart(cart);

    when(userRepository.findByUsername("username")).thenReturn(user);
    when(itemRepository.findById(0L)).thenReturn(Optional.of(items.get(0)));

    ResponseEntity<Cart> response = cartController.addToCart(req);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(itemsCount + 1, Objects.requireNonNull(response.getBody()).getItems().size());
  }

  @Test
  public void removeFromCartIsWorking() {
    ModifyCartRequest req = new ModifyCartRequest("username", 0, 1);
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));
    int itemsCount = items.size();
    User user = new User(0, "username", "password");
    Cart cart = new Cart(0L, user, items, BigDecimal.valueOf(0));
    user.setCart(cart);

    when(userRepository.findByUsername("username")).thenReturn(user);
    when(itemRepository.findById(0L)).thenReturn(Optional.of(items.get(0)));

    ResponseEntity<Cart> response = cartController.removeFromCart(req);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(itemsCount - 1, Objects.requireNonNull(response.getBody()).getItems().size());
  }


  @Test
  public void removeFromCartUnknownItemReturns404() {
    ModifyCartRequest req = new ModifyCartRequest("username", 0, 1);
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));
    User user = new User(0, "username", "password");
    Cart cart = new Cart(0L, user, items, BigDecimal.valueOf(0));
    user.setCart(cart);

    when(userRepository.findByUsername("test")).thenReturn(user);
    when(itemRepository.findById(0L)).thenReturn(Optional.empty());

    ResponseEntity<Cart> response = cartController.removeFromCart(req);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void removeFromCartUnknownUserReturns404() {
    ModifyCartRequest req = new ModifyCartRequest("username", 0, 1);
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));

    Cart cart = new Cart(0L, null, items, BigDecimal.valueOf(0));

    when(userRepository.findByUsername("username")).thenReturn(null);
    when(itemRepository.findById(0L)).thenReturn(Optional.of(items.get(0)));

    ResponseEntity<Cart> response = cartController.removeFromCart(req);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void submittingOrderForUnknownUserReturns404() {
    when(userRepository.findByUsername("username")).thenReturn(null);
    ResponseEntity<UserOrder> response = orderController.submit("username");
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void getOrdersForUnknownUserReturns404() {
    ModifyCartRequest req = new ModifyCartRequest("username", 0, 1);
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));

    Cart cart = new Cart();
    cart.setId(0L);
    cart.setItems(items);
    cart.setTotal(BigDecimal.valueOf(0));

    when(userRepository.findByUsername("username")).thenReturn(null);
    when(itemRepository.findById(0L)).thenReturn(Optional.of(items.get(0)));

    when(orderRepository.findByUser(null))
        .thenReturn(Collections.singletonList(UserOrder.createFromCart(cart)));
    ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("username");
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void getOrdersIsWorking() {
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));

    User user = new User(0, "username", "password");
    Cart cart = new Cart(0L, user, items, BigDecimal.valueOf(0));
    user.setCart(cart);

    when(userRepository.findByUsername("username")).thenReturn(null);
    when(itemRepository.findById(0L)).thenReturn(Optional.of(items.get(0)));

    when(orderRepository.findByUser(user))
        .thenReturn(Collections.singletonList(UserOrder.createFromCart(cart)));

    when(userRepository.findByUsername("username")).thenReturn(user);

    when(orderRepository.findByUser(user))
        .thenReturn(Collections.singletonList(UserOrder.createFromCart(cart)));
    ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("username");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    UserOrder firstOrder = Objects.requireNonNull(response.getBody()).get(0);
    assertEquals(items.size(), firstOrder.getItems().size());
    assertEquals(cart.getTotal(), firstOrder.getTotal());
  }

  @Test
  public void submittingOrderIsWorking() {
    List<Item> items = new ArrayList<>();
    items.add(new Item(0L, "WoW", "MMO RPG", BigDecimal.valueOf(39.99)));
    items.add(new Item(1L, "LoL", "Salt mine", BigDecimal.valueOf(666)));
    items.add(new Item(2L, "Hades", "Decent game", BigDecimal.valueOf(19.99)));

    User user = new User(0, "username", "password");
    Cart cart = new Cart(0L, user, items, BigDecimal.valueOf(0));
    user.setCart(cart);

    when(userRepository.findByUsername("username")).thenReturn(null);
    when(itemRepository.findById(0L)).thenReturn(Optional.of(items.get(0)));

    when(orderRepository.findByUser(user))
        .thenReturn(Collections.singletonList(UserOrder.createFromCart(cart)));

    when(userRepository.findByUsername("username")).thenReturn(user);

    when(orderRepository.findByUser(user))
        .thenReturn(Collections.singletonList(UserOrder.createFromCart(cart)));

    when(userRepository.findByUsername("username")).thenReturn(user);
    ResponseEntity<UserOrder> response = orderController.submit("username");
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(items.size(), Objects.requireNonNull(response.getBody()).getItems().size());
    assertEquals("username", response.getBody().getUser().getUsername());
    assertEquals(cart.getTotal(), response.getBody().getTotal());
  }

  @Test
  public void findByUsernameIsWorking() {
    User user = new User(0, "username", "password");
    Cart cart = new Cart(0L, user, new ArrayList<>(), BigDecimal.valueOf(0));
    user.setCart(cart);

    when(userRepository.findByUsername("username")).thenReturn(user);

    ResponseEntity<User> response = userController.findByUserName("username");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0L, Objects.requireNonNull(response.getBody()).getId());
    assertEquals("username", Objects.requireNonNull(response.getBody()).getUsername());
  }

  @Test
  public void creatingUserWithMismatchingConfirmationPasswordWillResult400() {
    CreateUserRequest request = new CreateUserRequest("username", "password", "missmatch");
    when(encoder.encode("password")).thenReturn("passwordhash");
    assertEquals(HttpStatus.BAD_REQUEST, userController.createUser(request).getStatusCode());
  }

  @Test
  public void creatingUserIsWorking() {
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("username");
    request.setPassword("password");
    request.setConfirmPassword("password");
    when(encoder.encode("password")).thenReturn("passwordhash");

    final ResponseEntity<User> response = userController.createUser(request);
    assertEquals(200, response.getStatusCodeValue());
    User user = Objects.requireNonNull(response.getBody());
    assertEquals(0, user.getId());
    assertEquals("username", user.getUsername());
    assertEquals("passwordhash", user.getPassword());
  }

  private <T> T getRequestBody(String json, Class<T> type) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(json, type);
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing json");
    }
  }

}
