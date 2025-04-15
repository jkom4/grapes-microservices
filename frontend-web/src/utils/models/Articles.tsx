// src/utils/models/Article.ts
import placeholder from "../../assets/images/fruit.png"

class Article {
    id: number;
    categoryId: number;
    familyId: number;
    name: string;
    description: string;
    priceKg: number;
    priceUnit: number;
    stockKg: number;
    stockUnit: number;
    origin: string;
    picturePath: string;
    rating?: number;

    constructor(
        id: number,
        categoryId: number,
        familyId: number,
        name: string,
        description: string,
        priceKg: number,
        priceUnit: number,
        stockKg: number,
        stockUnit: number,
        origin: string,
        picturePath: string,
        rating?: number
    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.familyId = familyId;
        this.name = name;
        this.description = description;
        this.priceKg = priceKg;
        this.priceUnit = priceUnit;
        this.stockKg = stockKg;
        this.stockUnit = stockUnit;
        this.origin = origin;
        this.picturePath = picturePath;
        this.rating = rating;
    }

    static parse(data: any): Article {
        return new Article(
            data.id,
            data.categoryId,
            data.familyId,
            data.name,
            data.description,
            data.priceKg,
            data.priceUnit,
            data.stockKg,
            data.stockUnit,
            data.origin,
            data.picturePath || placeholder,
            data.rating || 4
        );
    }
}

export default Article;
