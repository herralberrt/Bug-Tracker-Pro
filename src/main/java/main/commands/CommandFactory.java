package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CommandFactory {

    private CommandFactory() {}

    public static Command createCommand(ObjectNode node) {
        String command = node.get("command").asText();

        if (command.equals("reportTicket")) {
            return new ReportTicket(node);
        } else if (command.equals("viewTickets")) {
            return new ViewTickets(node);
        } else if (command.equals("lostInvestors")) {
            return new LostInvestors(node);
        } else if (command.equals("createMilestone")) {
            return new CreateMilestone(node);
        } else if (command.equals("viewMilestones")) {
            return new ViewMilestones(node);
        } else if (command.equals("assignTicket")) {
            return new AssignTicket(node);
        } else if (command.equals("undoAssignTicket")) {
            return new UndoAssignTicket(node);
        } else if (command.equals("viewAssignedTickets")) {
            return new ViewAssignedTickets(node);
        } else if (command.equals("addComment")) {
            return new AddComment(node);
        } else if (command.equals("undoAddComment")) {
            return new UndoAddComment(node);
        } else if (command.equals("changeStatus")) {
            return new ChangeStatus(node);
        } else if (command.equals("undoChangeStatus")) {
            return new UndoChangeStatus(node);
        } else if (command.equals("viewTicketHistory")) {
            return new ViewTicketHistory(node);
        } else if (command.equals("search")) {
            return new Search(node);
        } else if (command.equals("viewNotifications")) {
            return new ViewNotifications(node);
        } else if (command.equals("generateCustomerImpactReport")) {
            return new GenerateCustomerImpactReport(node);
        } else if (command.equals("generateTicketRiskReport")) {
            return new GenerateTicketRiskReport(node);
        } else if (command.equals("generateResolutionEfficiencyReport")) {
            return new GenerateResolutionEfficiencyReport(node);
        } else if (command.equals("appStabilityReport")) {
            return new AppStabilityReport(node);
        } else if (command.equals("generatePerformanceReport")) {
            return new GeneratePerformanceReport(node);
        } else {
            throw new IllegalArgumentException("Unknown command");
        }
    }
}
