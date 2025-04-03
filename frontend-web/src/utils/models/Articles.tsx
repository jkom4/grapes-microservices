// utils/models/Article.ts
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

}

export default Article;