import { UserInfo } from "./user";

export interface AdminUserInfo extends UserInfo {
    isActive: boolean;
    updateAt: string;
}