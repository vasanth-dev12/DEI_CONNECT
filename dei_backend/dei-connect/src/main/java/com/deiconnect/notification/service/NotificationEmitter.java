package com.deiconnect.notification.service;

import com.deiconnect.notification.enums.NotificationCategory;

public interface NotificationEmitter {

    void emit(String employeeId, NotificationCategory category, String message);
}
