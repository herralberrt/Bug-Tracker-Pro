package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;

public final class CommandFactory {

    private CommandFactory() {
    }

    public static Command createCommand(ObjectNode node, App app) {
        String commandName = node.get("command").asText();

        switch (commandName) {

            default:
                throw new IllegalArgumentException("Unknown command: " + commandName);
        }
    }
}
