package grapes.microservices.salesservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
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
              "name": "pomme modifying",
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
                .content(articleJson));

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
  
  // @Test
  //   void testGetAvailableArticles() throws Exception {
  //       mockMvc.perform(get("/articles/available"))
  //               .andExpect(status().isOk())
  //               .andExpect(jsonPath("$").isArray());
  //   }

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
          "articleId": 1009,
          "quantityKg": 2,
          "quantity": 1
        }
        """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCartContent_WhenCartExists_ShouldReturnItemsAndTotal() throws Exception {
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
          "articleId": 1009,
          "quantityKg": 2,
          "quantity": 1
        }
        """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/cart/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalPrice").isNumber());
    }

    @Test
    void testConfirmAndPayOrder_ShouldDecrementStockAndClearCart() throws Exception {
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
          "articleId": 1009,
          "quantityKg": 1.5,
          "quantity": 2
        }
        """, orderId);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemJson))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/confirm/" + orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/cart/pay/" + orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cart/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facturePath").exists())
                .andExpect(jsonPath("$.totalPrice").isNumber());
    }

}
