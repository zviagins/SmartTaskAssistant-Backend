package com.smarttaskassistant.notification.model;

public record NotificationRequest(
        String type,
        String notification,
        Boolean error) {

    public static NotificationRequest error(String type, String notification){
        return new NotificationRequest(type, notification, true);
    }

    public static NotificationRequest success(String type, String notification) {
        return new NotificationRequest(type, notification, false);
    }
}
