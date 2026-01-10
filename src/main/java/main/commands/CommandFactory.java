package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CommandFactory {

    private CommandFactory() {}

    public static Command createCommand(ObjectNode node) {
        String commandName = node.get("command").asText();

        switch (commandName) {
            case "changeStatus":
                return new ChangeStatus(node);

            case "undoChangeStatus":
                return new UndoChangeStatus(node);

            case "reportTicket":
                return new ReportTicket(node);

            case "viewTickets":
                return new ViewTickets(node);

            case "addComment":
                return new AddComment(node);

            case "undoAddComment":
                return new UndoAddComment(node);

            case "createMilestone":
                return new CreateMilestone(node);

            case "viewMilestones":
                return new ViewMilestones(node);

            case "assignTicket":
                return new AssignTicket(node);

            case "viewAssignedTickets":
                return new ViewAssignedTickets(node);

            case "viewTicketHistory":
                return new ViewTicketHistory(node);

            case "viewNotifications":
                return new ViewNotifications(node);

            case "search":
                return new Search(node);

            case "generatePerformanceReport":
                return new GeneratePerformanceReport(node);

            default:
                throw new IllegalArgumentException("Unknown command: " + commandName);
        }
    }
}
