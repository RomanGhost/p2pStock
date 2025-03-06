import { DealInfo } from "./deal";

export interface TaskInfo {
    id: number;
    dealInfo: DealInfo;
    managerLogin: string;
    confirmation: boolean | null;
    errorDescription: string;
    result: string;
    priorityName: string;
    updatedAt: string;
  }
  
  export interface TaskPatchResultInfo {
    result: string;
  }
  