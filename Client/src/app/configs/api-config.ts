import { environment } from "../environment";

export const API_CONFIG = {
  apiUrl: `${environment.apiHost}/api/v1/p2pstock`,
  wsUrl: `${environment.wsHost}/ws/v1/p2pstock`
};

export const CHAT_API_CONFIG = {
  apiUrl: `${environment.apiHostChat}/api/v1/chat`, 
  wsUrl: `${environment.apiHostChat}/ws/v1/chat`
};