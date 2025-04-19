import placeholder from "../../assets/images/fruit.png";

interface CartItem {
    id: number;
    articleId: number;
    articleName: string;
    picturePath: string | null;
    quantityKg: number;
    quantity: number;
    price: number;
}

class CartItemModel implements CartItem {
    id: number;
    articleId: number;
    articleName: string;
    picturePath: string | null;
    quantityKg: number;
    quantity: number;
    price: number;

    constructor(
        id: number,
        articleId: number,
        articleName: string,
        picturePath: string | null,
        quantityKg: number,
        quantity: number,
        price: number
    ) {
        this.id = id;
        this.articleId = articleId;
        this.articleName = articleName;
        this.picturePath = picturePath;
        this.quantityKg = quantityKg;
        this.quantity = quantity;
        this.price = price;
    }

    static parse(data: any): CartItemModel {
        return new CartItemModel(
            data.id,
            data.articleId,
            data.articleName,
            data.picturePath || placeholder,
            data.quantityKg,
            data.quantity,
            data.price
        );
    }
}

export default CartItemModel;
