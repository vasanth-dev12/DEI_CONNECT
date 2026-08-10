package com.deiconnect.survey.client;

public record EmitNotificationRequest(
        String employeeId,
        String category,
        String message
) {

    public static final class NotificationCategory {
        public static final String SURVEY = "SURVEY";

        private NotificationCategory() {
        }
    }
}
