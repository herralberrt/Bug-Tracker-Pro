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
        } else {
            throw new IllegalArgumentException("Unknown command");
        }
    }
}
