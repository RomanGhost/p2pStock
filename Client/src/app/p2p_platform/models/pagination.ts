export interface PageInfo{
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
}

export interface PaginationResponse<T> {
    content: T[];   // Содержит массив заказов
    page: PageInfo;
}