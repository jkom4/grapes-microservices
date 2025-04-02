package grapes.microservices.salesservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

}
