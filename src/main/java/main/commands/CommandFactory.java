package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CommandFactory {

    private CommandFactory() {}

    public static Command createCommand(ObjectNode node) {
        return switch (node.get("command").asText()) {
            case "reportTicket" -> new ReportTicket(node);
            case "viewTickets" -> new ViewTickets(node);
            case "lostInvestors" -> new LostInvestors(node);
            case "createMilestone" -> new CreateMilestone(node);
            case "viewMilestones" -> new ViewMilestones(node);
            case "assignTicket" -> new AssignTicket(node);
            case "undoAssignTicket" -> new UndoAssignTicket(node);
            case "viewAssignedTickets" -> new ViewAssignedTickets(node);
            default -> throw new IllegalArgumentException("Unknown command");
        };
    }
}
