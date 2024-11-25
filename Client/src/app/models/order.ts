export interface OrderInfo {
    id: number;
    userLogin: string;
    walletId: number;
    cryptocurrencyCode:string;
    cardId: number;
    typeName: string;
    statusName: string;
    unitPrice: number;
    quantity: number;
    description: string;
    createdAt: string; // ISO string format
    lastStatusChange: string; // ISO string format
  }
export interface CreateOrderInfo {
    walletId: number;
    cardId: number;
    typeName: string;
    statusName: string;
    unitPrice: number;
    quantity: number;
    description: string;
}
  