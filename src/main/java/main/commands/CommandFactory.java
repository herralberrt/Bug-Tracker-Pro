package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class CommandFactory {

    private CommandFactory() {
    }

    public static Command createCommand(ObjectNode node) {
        String commandName = node.get("command").asText();

        switch (commandName) {
            case "changeStatus":
                return new ChangeStatus(node);

            case "undoChangeStatus":
                return new UndoChangeStatus(node);

            default:
                throw new IllegalArgumentException("Unknown command: " + commandName);
        }
    }
}
