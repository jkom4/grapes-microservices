package grapes.microservices.salesservice;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
class TransactionsServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllArticles() throws Exception {
        mockMvc.perform(get("/clm/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }


    @Test
    void testSearchExistingArticleByName() throws Exception {
        String articleJson = """
        {
         "categoryId": 1,
          "familyId": 1,
          "name": "Kiwi",
          "description": "fruit vitaminé",
          "priceKg": 2.99,
          "priceUnit": 1.50,
          "stockKg": 10.0,
          "stockUnit": 5.0,
          "origin": "Nouvelle-Zélande",
          "picturePath": "kiwi.jpg"
        }
    """;

        // 👉 Crée l'article avant la recherche
        mockMvc.perform(post("/clm/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isCreated());

        // 👉 Ensuite recherche
        mockMvc.perform(get("/clm/articles/search")
                        .param("name", "Kiwi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Kiwi"));
    }

    @Test
    void testSearchNonExistingArticle() throws Exception {
        mockMvc.perform(get("/clm/articles/search")
                        .param("name", "Abricot"))  // Abricot n'existe pas en base
                .andExpect(status().isNotFound())
                .andExpect(content().string("No items found with the name: 'Abricot'"));
    }

    @Test
    void testSearchArticleWithEmptyName() throws Exception {
        mockMvc.perform(get("/clm/articles/search")
                        .param("name", " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The 'name' field cannot be empty."));
    }

    @Test
    void testGetAvailableArticlesWithoutPagination() throws Exception {
        mockMvc.perform(get("/clm/articles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testCreateArticle() throws Exception {
        String articleJson = """
            {
              "categoryId": 1,
              "familyId": 1,
              "name": "Abricot",
              "description": "Fruit orange sucré",
              "priceKg": 4.50,
              "priceUnit": 1.00,
              "stockKg": 5.0,
              "stockUnit": 10.0,
              "origin": "France",
              "picturePath": "abricot.jpg"
            }
        """;

        mockMvc.perform(post("/clm/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Abricot"));
    }

    @Test
    void testUpdateArticle() throws Exception {
        String updateJson = """
            {
              "categoryId": 1,
              "familyId": 1,
              "name": "Kiwi Modifié",
              "description": "Un kiwi encore meilleur",
              "priceKg": 5.00,
              "priceUnit": 2.00,
              "stockKg": 12.0,
              "stockUnit": 6.0,
              "origin": "Nouvelle-Zélande",
              "picturePath": "kiwi_modif.jpg"
            }
        """;

        mockMvc.perform(put("/clm/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kiwi Modifié"));
    }


    /*
    @Test
    void testDeliveryTriggeredAfterOrderPayment() throws Exception {
        String familyJson = """
        {
          "name": "Fruits"
        }
    """;
        mockMvc.perform(post("/clm/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(familyJson))
                .andExpect(status().isOk());

        String categoryJson = """
        {
          "name": "Fruits Exotiques"
        }
    """;
        mockMvc.perform(post("/clm/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson))
                .andExpect(status().isOk());

        String articleJson = """
        {
          "categoryId": 1,
          "familyId": 1,
          "name": "Kiwi",
          "description": "fruit ",
          "priceKg": 2.99,
          "priceUnit": 1.50,
          "stockKg": 10.0,
          "stockUnit": 5.0,
          "origin": "Nouvelle-Zélande",
          "picturePath": "kiwi.jpg"
        }
    """;
        mockMvc.perform(post("/clm/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isCreated());

        MvcResult initResult = mockMvc.perform(post("/cll/cart/init"))
                .andExpect(status().isOk())
                .andReturn();

        String initContent = initResult.getResponse().getContentAsString();
        Long orderId = JsonPath.read(initContent, "$.orderId");

        mockMvc.perform(post("/cll/cart/add")
                        .param("articleId", "1") // ID 1 de ton article Kiwi
                        .param("quantity", "2")
                        .param("orderId", orderId.toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cll/cart/pay/" + orderId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/cll/deliveries/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId));
    }
*/

    @Test
    void testGetArticleById() throws Exception {
        String articleJson = """
        {
          "categoryId": 1,
          "familyId": 1,
          "name": "Banane",
          "description": "Fruit jaune énergétique",
          "priceKg": 2.8,
          "priceUnit": 0.7,
          "stockKg": 10,
          "stockUnit": 10,
          "origin": "Colombie",
          "picturePath": "banane.jpg"
        }
    """;

        MvcResult result = mockMvc.perform(post("/clm/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer createdArticleId = JsonPath.read(response, "$.id");

        mockMvc.perform(get("/clm/articles/clm/articles/{id}", createdArticleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdArticleId))
                .andExpect(jsonPath("$.name").value("Banane"))
                .andExpect(jsonPath("$.origin").value("Colombie"));
    }



}