package main.notif;

import java.time.LocalDate;

public final class NotificationFactory {

    private NotificationFactory() {

    }

    /**
     * Creates a notification for ticket creation
     */
    public static Notification createTicket(final int ticketId,
                                            final String type,
                                            final String reportedBy) {

        String rep = (reportedBy == null || reportedBy.isEmpty()) ? "Anonymous" : reportedBy;
        return new Notification("A new ticket (ID: " + ticketId + ", type: "
                + type + ") has been created by " + rep + ".",
                NotificationType.TICKET_CREATED);
    }

    /**
     * Creates a notification for milestone creation
     */
    public static Notification createMilestone(final String milestoneName,
                                               final LocalDate dueDate) {
        return new Notification("New milestone "
                + milestoneName + " has been created with due date " + dueDate + ".",
                NotificationType.MILESTONE_CREATED);
    }
}
