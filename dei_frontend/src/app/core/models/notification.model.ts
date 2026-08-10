import { NotificationCategory, NotificationStatus } from './enums';

export interface EmitNotificationRequest {
  employeeId: string;
  category: NotificationCategory;
  message: string;
}

export interface NotificationResponse {
  notificationId: number;
  employeeId: string;
  message: string;
  category: NotificationCategory;
  status: NotificationStatus;
  createdDate: string;
}
