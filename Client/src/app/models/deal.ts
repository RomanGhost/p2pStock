import { OrderInfo } from "./order";

export interface DealInfo{
    id:number;
    buyOrder: OrderInfo;
    sellOrder: OrderInfo;
    statusName: string;
    createdAt: string;
    lastStatusChange: string;
}

export interface CreateDealInfo{
    walletId: number;
    cardId: number;
    counterpartyOrderId: number;
}