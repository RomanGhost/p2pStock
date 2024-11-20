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
  // С пагинацией
  export interface OrderListResponse {
    content: OrderInfo[];   // Содержит массив заказов
    totalElements: number;   // Общее количество заказов
    totalPages: number;      // Количество страниц
    size: number;            // Размер страницы
    number: number;          // Номер текущей страницы
    last: boolean;           // Последняя страница
    first: boolean;          // Первая страница
    numberOfElements: number; // Количество элементов на текущей странице
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
  