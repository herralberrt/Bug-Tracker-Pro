package main.notif;

import java.time.LocalDate;

public final class NotificationFactory {

    private NotificationFactory() {
    }

    public static Notification milestoneCreated(String milestoneName,
            LocalDate dueDate) {
        return new Notification("New milestone " + milestoneName +
                " has been created with due date " + dueDate + ".");
    }

    public static Notification milestoneDueTomorrow(String milestoneName) {
        return new Notification("Milestone " + milestoneName +
                        " is due tomorrow. All unresolved tickets are now CRITICAL.");
    }

    public static Notification milestoneUnblocked(String milestoneName, int ticketId) {
        return new Notification("Milestone " + milestoneName +
                        " is now unblocked as ticket " + ticketId + " has been CLOSED.");
    }

    public static Notification milestoneUnblockedAfterDue(String milestoneName) {
        return new Notification("Milestone " + milestoneName +
                        " was unblocked after due date. All active tickets are now CRITICAL.");
    }
}
