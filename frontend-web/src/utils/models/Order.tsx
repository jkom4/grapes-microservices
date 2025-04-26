export default class Order {
    id: number;
    code: number;
    userId: string;
    facturePath: string | null;
    totalPrice: number | null;
    createdAt: string;
    finished: boolean;
    paid: boolean;

    constructor(data: any) {
        this.id = data.id;
        this.code = data.code;
        this.userId = data.userId;
        this.facturePath = data.facturePath;
        this.totalPrice = data.totalPrice;
        this.createdAt = data.createdAt;
        this.finished = data.finished;
        this.paid = data.paid;
    }

    static parse(data: any): Order {
        return new Order(data);
    }
}