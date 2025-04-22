export interface Order {
    id: number;
    code: number;
    userId: number;
    facturePath: string | null;
    totalPrice: number | null;
    createdAt: string;
    finished: boolean;
    paid: boolean;
}

export interface SortConfig {
    key: keyof Order;
    direction: 'asc' | 'desc';
}