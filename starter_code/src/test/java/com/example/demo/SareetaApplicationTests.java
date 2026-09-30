package com.example.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class SareetaApplicationTests {

	private CartController cartController;
	private ItemController itemController;
	private OrderController orderController;
	private UserController userController;

	private CartRepository cartRepo = mock(CartRepository.class);
	private ItemRepository itemRepo = mock(ItemRepository.class);
	private OrderRepository orderRepo = mock(OrderRepository.class);
	private UserRepository userRepo = mock(UserRepository.class);
	private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
	
	// given test by udacity
	@Test
	public void contextLoads() {}
	// -- rm --
	private ResponseEntity<User> createUser(String name, String pwd, String confirmPwd){
		CreateUserRequest r = new CreateUserRequest();
		r.setUsername(name);
		r.setPassword(pwd);
		r.setConfirmPassword(confirmPwd);
		
		return userController.createUser(r);
	}

	@BeforeEach 
	public void setUp() {
		cartController = new CartController();
		TestUtils.injectObjects(cartController, "userRepository", userRepo);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepo);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepo);

		itemController = new ItemController();
		TestUtils.injectObjects(itemController, "itemRepository", itemRepo);

		orderController = new OrderController();
		TestUtils.injectObjects(orderController, "userRepository", userRepo);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepo);

		userController = new UserController();
		TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);
	}

	@Test
	public void createUserSuccess_SUCCESS() throws Exception {
		when(encoder.encode("correctPassword")).thenReturn("hashedPassword");		
		final ResponseEntity<User> response = createUser("test", "correctPassword", "correctPassword");
		assertNotNull(response);
		// response.getStatusCodeValue() from tutorial is deprecated!
		assertEquals(HttpStatus.OK, response.getStatusCode());

		User user = response.getBody();
		
		assertNotNull(user);
		assertEquals(0, user.getId());
		assertEquals("test", user.getUsername());
		assertEquals("hashedPassword", user.getPassword());
	}

	@Test
	public void createUserSuccess_FAIL_PasswordTooShort() throws Exception {
		final ResponseEntity<User> response = createUser("test", "short", "short");
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void createUserSuccess_FAIL_PasswordNotConfirmed() throws Exception {
		final ResponseEntity<User> response = createUser("test", "correctPassword", "wrongPassword");
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test 
	public void addToCart() throws Exception {
		User user = new User();
		Cart cart = new Cart();
		user.setUsername("test");
		user.setPassword("password");
		cart.setUser(user);
		user.setCart(cart);
		when(userRepo.findByUsername("test")).thenReturn(user);
		 
		Item item = new Item();
		item.setId(1L);
		item.setName("test item");
		item.setPrice(new BigDecimal("123.45"));
		item.setDescription("A very detailed description.");
		when(itemRepo.findById(1L)).thenReturn(Optional.of(item));
		
		ModifyCartRequest r = new ModifyCartRequest();
		r.setItemId(1L);
		r.setQuantity(15);
		r.setUsername("test");

		final ResponseEntity<Cart> response = cartController.addTocart(r);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Cart responseCart = response.getBody();
		assertNotNull(responseCart);
		assertEquals(user, responseCart.getUser());
		assertEquals(
			new BigDecimal("123.45").multiply(new BigDecimal(15)), 
			responseCart.getTotal()
		);
		
		List<Item> items = responseCart.getItems(); 
		assertNotNull(items);
		assertEquals(15, items.size());
		for(int i=0; i<15; i++){
			assertEquals(item.getId(), items.get(i).getId());
			assertEquals(item.getName(), items.get(i).getName());
			assertEquals(item.getPrice(), items.get(i).getPrice());
			assertEquals(item.getDescription(), items.get(i).getDescription());
		}	
	}

	@Test 
	public void addToCart_FAIL_UserNotFound() throws Exception {
		when(userRepo.findByUsername("test")).thenReturn(null);

		ModifyCartRequest r = new ModifyCartRequest();
		r.setItemId(1L);
		r.setQuantity(15);
		r.setUsername("test");

		final ResponseEntity<Cart> response = cartController.addTocart(r);
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

		@Test 
	public void removeFromCart() throws Exception {
		Item item = new Item();
		item.setId(1L);
		item.setName("test item");
		item.setPrice(new BigDecimal("123.45"));
		item.setDescription("A very detailed description.");
		when(itemRepo.findById(1L)).thenReturn(Optional.of(item));
		
		User user = new User();
		Cart cart = new Cart();
		user.setUsername("test");
		user.setPassword("password");
		cart.setUser(user);
		for(int i = 0; i < 51; i++){
			cart.addItem(item);
		}
		user.setCart(cart);
		when(userRepo.findByUsername("test")).thenReturn(user);

		ModifyCartRequest r = new ModifyCartRequest();
		r.setItemId(1L);
		r.setQuantity(15);
		r.setUsername("test");

		final ResponseEntity<Cart> response = cartController.removeFromcart(r);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		Cart responseCart = response.getBody();
		assertNotNull(responseCart);
		assertEquals(user, responseCart.getUser());
		assertEquals(
			new BigDecimal("123.45").multiply(new BigDecimal(36)), 
			responseCart.getTotal()
		);
		
		List<Item> items = responseCart.getItems(); 
		assertNotNull(items);
		assertEquals(36, items.size());
		for(int i=0; i<36; i++){
			assertEquals(item.getId(), items.get(i).getId());
			assertEquals(item.getName(), items.get(i).getName());
			assertEquals(item.getPrice(), items.get(i).getPrice());
			assertEquals(item.getDescription(), items.get(i).getDescription());
		}	
	}

	@Test 
	public void removeFromCart_FAIL_UserNotFound() throws Exception {
		User user = new User();
		user.setUsername("test");
		when(userRepo.findByUsername("test")).thenReturn(null);

		ModifyCartRequest r = new ModifyCartRequest();
		final ResponseEntity<Cart> response = cartController.removeFromcart(r);
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test 
	public void removeFromCart_FAIL_ItemNotFound() throws Exception {
		Item item = new Item();
		item.setId(1L);
		when(itemRepo.findById(1L)).thenReturn(Optional.empty());
		
		User user = new User();
		user.setUsername("test");
		when(userRepo.findByUsername("test")).thenReturn(user);

		ModifyCartRequest r = new ModifyCartRequest();
		r.setItemId(1L);
		r.setUsername("test");

		final ResponseEntity<Cart> response = cartController.removeFromcart(r);
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
	@Test 
	public void submitOrder() throws Exception {
		Item item = new Item();
		item.setId(1L);
		item.setName("test item");
		item.setPrice(new BigDecimal("123.45"));
		item.setDescription("A very detailed description.");
		
		User user = new User();
		Cart cart = new Cart();
		user.setUsername("test");
		user.setPassword("password");
		cart.setUser(user);
		for(int i = 0; i < 36; i++){
			cart.addItem(item);
		}
		user.setCart(cart);
		when(userRepo.findByUsername("test")).thenReturn(user);
		
		final ResponseEntity<UserOrder> response = orderController.submit("test");
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		UserOrder responseCart = response.getBody();
		assertNotNull(responseCart);
		assertEquals(user, responseCart.getUser());
		
		List<Item> items = responseCart.getItems(); 
		assertNotNull(items);
		assertEquals(36, items.size());
		for(int i=0; i<36; i++){
			assertEquals(item.getId(), items.get(i).getId());
			assertEquals(item.getName(), items.get(i).getName());
			assertEquals(item.getPrice(), items.get(i).getPrice());
			assertEquals(item.getDescription(), items.get(i).getDescription());
		}
		
		assertEquals(
			new BigDecimal("123.45").multiply(new BigDecimal(36)), 
			responseCart.getTotal()
		);
	}
	
	@Test 
	public void submitOrder_FAIL_userNotFound() throws Exception {
		User user = new User();
		user.setUsername("test");
		when(userRepo.findByUsername("test")).thenReturn(null);
		
		final ResponseEntity<UserOrder> response = orderController.submit("test");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test 
	public void getOrdersForUser() throws Exception {
		User user = new User();
		Cart cart = new Cart();
		user.setUsername("test");
		user.setPassword("password");
		cart.setUser(user);
		user.setCart(cart);
		when(userRepo.findByUsername("test")).thenReturn(user);

		UserOrder order = new UserOrder();
		order.setUser(user);
		order.setItems(List.of(new Item(), new Item(), new Item()));
		order.setTotal(new BigDecimal(123));
		when(orderRepo.findByUser(user)).thenReturn(List.of(order));
		
		final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("test");
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<UserOrder> orderHistory = response.getBody();
		assertNotNull(orderHistory);
		assertEquals(1, orderHistory.size());
		assertEquals(user, orderHistory.get(0).getUser());
		assertEquals(new BigDecimal(123), orderHistory.get(0).getTotal());
		assertEquals(3, orderHistory.get(0).getItems().size());
	}
	
	@Test 
	public void getOrdersForUser_FAIL_userNotFound() throws Exception {
		User user = new User();
		user.setUsername("test");
		when(userRepo.findByUsername("test")).thenReturn(null);
		
		final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("test");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test 
	public void getItems() throws Exception {
		Item item1 = new Item();
		item1.setName("item1");
		item1.setPrice(new BigDecimal(12));
		item1.setId(1L);
		item1.setDescription("aaaaaaaaaaa");

		Item item2 = new Item();
		item2.setName("item2");
		item2.setPrice(new BigDecimal(20));
		item2.setId(2L);
		item2.setDescription("bbbbbbbbbbb");

		List<Item> items = List.of(item1, item2); 

		when(itemRepo.findAll()).thenReturn(items);
		ResponseEntity<List<Item>> response = itemController.getItems();

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<Item> responseItems = response.getBody();
		for(int i = 0; i< items.size(); i++){
			Item expectedItem = items.get(i);
			Item receivedItem = responseItems.get(i);

			assertEquals(expectedItem.getDescription(), receivedItem.getDescription());
			assertEquals(expectedItem.getId(), receivedItem.getId());
			assertEquals(expectedItem.getName(), receivedItem.getName());
			assertEquals(expectedItem.getPrice(), receivedItem.getPrice());
		}
	}

	@Test 
	public void getItemsById() throws Exception {
		Item item = new Item();
		item.setName("item");
		item.setPrice(new BigDecimal(12));
		item.setId(1L);
		item.setDescription("aaaaaaaaaaa");

		when(itemRepo.findById(item.getId())).thenReturn(Optional.of(item));
		ResponseEntity<Item> response = itemController.getItemById(item.getId());

		assertNotNull(response);
		Item receivedItem = response.getBody();
		assertEquals(item.getDescription(), receivedItem.getDescription());
		assertEquals(item.getId(), receivedItem.getId());
		assertEquals(item.getName(), receivedItem.getName());
		assertEquals(item.getPrice(), receivedItem.getPrice());
	}

	@Test 
	public void getItemsByName() throws Exception {
		Item item = new Item();
		item.setName("item");
		item.setPrice(new BigDecimal(12));
		item.setId(1L);
		item.setDescription("aaaaaaaaaaa");

		when(itemRepo.findByName(item.getName())).thenReturn(List.of(item));
		ResponseEntity<List<Item>> response = itemController.getItemsByName(item.getName());

		assertNotNull(response);
		List<Item> receivedItem = response.getBody();
		assertNotNull(receivedItem);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, receivedItem.size());
		assertEquals(item.getDescription(), receivedItem.get(0).getDescription());
		assertEquals(item.getId(), receivedItem.get(0).getId());
		assertEquals(item.getName(), receivedItem.get(0).getName());
		assertEquals(item.getPrice(), receivedItem.get(0).getPrice());
	}

	@Test 
	public void getItemsByName_FAIL_notFound() throws Exception {
		when(itemRepo.findByName("nonKnown")).thenReturn(new ArrayList<Item>());
		ResponseEntity<List<Item>> response = itemController.getItemsByName("nonKnown");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		when(itemRepo.findByName("nonKnown")).thenReturn(null);
		response = itemController.getItemsByName("nonKnown");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test 
	public void findById() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("user");
		user.setPassword("password");
		
		when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
		ResponseEntity<User> response = userController.findById(user.getId());

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		User receivedUser = response.getBody();
		assertEquals(user.getId(), receivedUser.getId());
		assertEquals(user.getPassword(), receivedUser.getPassword());
		assertEquals(user.getUsername(), receivedUser.getUsername());
	}

	@Test 
	public void findByUserName() throws Exception {
		User user = new User();
		user.setUsername("user");
		user.setPassword("password");
		
		when(userRepo.findByUsername(user.getUsername())).thenReturn(user);
		ResponseEntity<User> response = userController.findByUserName(user.getUsername());

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		User receivedUser = response.getBody();
		assertEquals(user.getPassword(), receivedUser.getPassword());
		assertEquals(user.getUsername(), receivedUser.getUsername());
	}

	@Test 
	public void findByUserName_FAIL_notFound() throws Exception {
		when(userRepo.findByUsername("")).thenReturn(null);
		ResponseEntity<User> response = userController.findByUserName("");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
}
