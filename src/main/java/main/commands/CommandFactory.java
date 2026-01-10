package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CommandFactory {

    private CommandFactory() {}

    public static Command createCommand(ObjectNode node) {
        return switch (node.get("command").asText()) {
            case "reportTicket" -> new ReportTicket(node);
            case "viewTickets" -> new ViewTickets(node);
            case "lostInvestors" -> new LostInvestors(node);
            default -> throw new IllegalArgumentException("Unknown command");
        };
    }
}
