package main.notif;

public final class Notification {
    private final String message;
    private final NotificationType type;

    public Notification(final String message, final NotificationType type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }
}
