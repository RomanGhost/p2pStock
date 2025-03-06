export interface ChatInfo {
  chatId: number;
  firstLogin: string;
  secondLogin: string;
  dealId: number;
}

export interface ChatList {
  chatList: ChatInfo[];
  time: number;
}