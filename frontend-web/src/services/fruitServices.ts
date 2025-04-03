// src/services/fruitService.ts
import Article from "../utils/models/Articles"; // Import the Article model
import placeholder from "../assets/images/fruit.png"; // Fallback image

const url = "http://localhost:8092";

const fetchFruits = async (limit: number = 3): Promise<Article[]> => {
    try {
        const response = await fetch(`${url}/clm/articles`);

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status} - ${response.statusText}`);
        }

        const text = await response.text();

        const contentType = response.headers.get("Content-Type");
        if (!contentType || !contentType.includes("application/json")) {
            throw new Error(`Response is not JSON: ${text.slice(0, 100)}...`);
        }

        const data = JSON.parse(text);
        // Create instances of Article and limit the number of fruits
        return data.slice(0, limit).map((item: any) =>
            new Article(
                item.id,
                item.categoryId,
                item.familyId,
                item.name,
                item.description,
                item.priceKg,
                item.priceUnit,
                item.stockKg,
                item.stockUnit,
                item.origin,
                item.picturePath || placeholder, // Fallback to placeholder
                item.rating || 4 // Default value for rating
            )
        );
    } catch (err) {
        throw new Error(err instanceof Error ? err.message : "An error occurred");
    }
};

export default fetchFruits;
