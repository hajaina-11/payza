package io.granix.notification.domain.model;

public enum NotificationChannel {
    SMS("sms", "SMS", 1),
    EMAIL("email", "Email", 2),
    PUSH("push", "Notification Push", 3),
    IN_APP("in_app", "Notification Application", 0);

    private final String code;
    private final String description;
    private final int priority;

    NotificationChannel(String code, String description, int priority) {
        this.code = code;
        this.description = description;
        this.priority = priority;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public int getPriority() { return priority; }
}