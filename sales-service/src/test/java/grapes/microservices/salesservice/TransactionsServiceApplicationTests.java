package grapes.microservices.salesservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionsServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateArticle() throws Exception {
        String articleJson = """
            {
              "categoryId": 1,
              "familyId": 1,
              "name": "kiwi",
              "description": "fruit vitaminé",
              "priceKg": 2.99,
              "priceUnit": 1.50,
              "stockKg": 10.0,
              "stockUnit": 5.0,
              "origin": "Nouvelle-Zélande",
              "picturePath": "kiwi.jpg"
            }
        """;

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("kiwi"));
    }

    @Test
    void testGetAllArticles() throws Exception {
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testUpdateArticle() throws Exception {
        String updatedJson = """
            {
              "categoryId": 1,
              "familyId": 1,
              "name": "pomme modifiée",
              "description": "modif test",
              "priceKg": 3.99,
              "priceUnit": 2.49,
              "stockKg": 12.00,
              "stockUnit": 6.00,
              "origin": "France",
              "picturePath": "pomme_modif.jpg"
            }
        """;

        mockMvc.perform(put("/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("pomme modifying"));
    }

    @Test
    void testSearchArticleByName() throws Exception {
        mockMvc.perform(get("/articles/search")
                        .param("name", "kiwi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("kiwi"));
    }

    @Test
    void testSearchArticleWithEmptyName() throws Exception {
        mockMvc.perform(get("/articles/search")
                        .param("name", " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The 'name' field cannot be empty."));
    }

    @Test
    void testSearchArticleNotFound() throws Exception {
        mockMvc.perform(get("/articles/search")
                        .param("name", "mangue"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No items found with the name: 'mangue'"));
    }

    @Test
    void testGetAvailableArticles() throws Exception {
        mockMvc.perform(get("/articles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAvailableArticlesWithPagination() throws Exception {
        mockMvc.perform(get("/articles/available")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[5]").doesNotExist()) //  5 results
                .andExpect(jsonPath("$.content[0].stockKg").isNumber())
                .andExpect(jsonPath("$.content[0].stockUnit").isNumber());
    }

    @Test
    void testInitCartAndAddToCart_WithAnanas() throws Exception {
        // Step 1 – Create temporary order (cart)
        String orderJson = """
        {
          "userId": 1
        }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        // Step 2 – Add "Ananas" article (id = 1009)
        String addItemJson = String.format("""
        {
          "orderId": %s,
          "articleId": 1009,
          "quantityKg": 2,
          "quantity": 1
        }
    """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(Integer.parseInt(orderId)))
                .andExpect(jsonPath("$.articleId").value(1009))
                .andExpect(jsonPath("$.quantityKg").value(2))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void testAddToCart_ExceedsAvailableStock_WithAnanas_ShouldReturnBadRequest() throws Exception {
        String orderJson = """
        {
          "userId": 1
        }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        // Try to add more kg than available (e.g., 9999 vs 15)
        String addItemJson = String.format("""
        {
          "orderId": %s,
          "articleId": 1009,
          "quantityKg": 9999,
          "quantity": null
        }
    """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Not enough stock in kg")));
    }
    @Test
    void testGetCartContent_WhenCartExists_ShouldReturnItemsAndTotal() throws Exception {
        // Step 1 – Create a temporary order (cart)
        String orderJson = """
    {
      "userId": 1
    }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        // Step 2 – Add a valid article to the cart
        String addItemJson = String.format("""
    {
      "orderId": %s,
      "articleId": 1009,
      "quantityKg": 2,
      "quantity": 1
    }
    """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isOk());

        // Step 3 – Retrieve the cart and verify its content and total price
        mockMvc.perform(get("/cart/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].articleId").isNumber())
                .andExpect(jsonPath("$.items[0].articleName").isString())
                .andExpect(jsonPath("$.totalPrice").isNumber());
    }

    @Test
    void testRemoveItemFromCart_ShouldSucceedAndCartShouldBeEmpty() throws Exception {
        // Step 1 – Init temporary order
        String orderJson = """
    {
      "userId": 1
    }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        // Step 2 – Add item to cart
        String addItemJson = String.format("""
    {
      "orderId": %s,
      "articleId": 1009,
      "quantityKg": 1.5,
      "quantity": 1
    }
    """, orderId);

        String addedItemJson = mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract itemId from the response
        String itemId = addedItemJson.replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");

        // Step 3 – Delete the item
        mockMvc.perform(delete("/cart/remove/" + itemId))
                .andExpect(status().isNoContent());

        // Step 4 – Check cart is now empty
        mockMvc.perform(get("/cart/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    void testRemoveNonExistentCartItem_ShouldReturnBadRequest() throws Exception {
        // Step – Try to delete a non-existing cart item
        int fakeItemId = 999999;

        mockMvc.perform(delete("/cart/remove/" + fakeItemId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cart item not found")));
    }

    @Test
    void testClearCart_ShouldRemoveAllItemsFromOrder() throws Exception {
        // Step 1 – Create a temporary order
        String orderJson = """
    {
      "userId": 1
    }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        // Step 2 – Add two articles to the cart
        String item1 = String.format("""
    {
      "orderId": %s,
      "articleId": 1007,
      "quantityKg": 1,
      "quantity": 1
    }
    """, orderId);

        String item2 = String.format("""
    {
      "orderId": %s,
      "articleId": 1009,
      "quantityKg": null,
      "quantity": 2
    }
    """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(item1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(item2))
                .andExpect(status().isOk());

        // Step 3 – Clear the cart
        mockMvc.perform(delete("/cart/clear/" + orderId))
                .andExpect(status().isNoContent());

        // Step 4 – Confirm the cart is empty
        mockMvc.perform(get("/cart/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    void testConfirmAndPayOrder_ShouldDecrementStockAndClearCart() throws Exception {
        // Step 1 – Create a temporary order
        String orderJson = """
    {
      "userId": 1
    }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        // Step 2 – Add one item to cart (e.g. articleId = 1009)
        String addItemJson = String.format("""
    {
      "orderId": %s,
      "articleId": 1009,
      "quantityKg": 1.5,
      "quantity": 2
    }
    """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isOk());

        // Step 3 – Confirm the cart (check stock)
        mockMvc.perform(post("/cart/confirm/" + orderId))
                .andExpect(status().isNoContent());

        // Step 4 – Simulate payment → stock -1.5kg / -2 unit
        mockMvc.perform(post("/cart/pay/" + orderId))
                .andExpect(status().isNoContent());

        // Step 5 – Verify cart is empty
        mockMvc.perform(get("/cart/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        // Step 6 – Retrieve order and verify invoice + totalPrice
        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facturePath").exists())
                .andExpect(jsonPath("$.totalPrice").isNumber());

    }

    @Test
    void testConfirmOrderWithEmptyCart_ShouldReturnBadRequest() throws Exception {
        String orderJson = """
    {
      "userId": 1
    }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        mockMvc.perform(post("/cart/confirm/" + orderId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("empty cart")));
    }

    @Test
    void testAddToCartWithInvalidArticle_ShouldReturnBadRequest() throws Exception {
        String orderJson = """
    {
      "userId": 1
    }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        String addItemJson = String.format("""
    {
      "orderId": %s,
      "articleId": 999999,
      "quantityKg": 1,
      "quantity": null
    }
    """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Article not found")));
    }


    @Test
    void testPaymentFailsDueToStockChange_ShouldReturnBadRequest() throws Exception {
        String orderJson = """
    {
      "userId": 1
    }
    """;

        String orderId = mockMvc.perform(post("/cart/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("[^0-9]", "");

        String addItemJson = String.format("""
    {
      "orderId": %s,
      "articleId": 1007,
      "quantityKg": 2,
      "quantity": 1
    }
    """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isOk());

        // Simulates that someone has emptied the stock in the base at this time

        mockMvc.perform(post("/cart/pay/" + orderId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Insufficient stock")));
    }



}
