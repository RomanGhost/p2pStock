export interface Message {
    id: number;
    senderUserLogin: string;
    message: string;
    chatId: number;
    time: number;
}

export interface MessageList {
    messages: Message[];
    time: number;
}
