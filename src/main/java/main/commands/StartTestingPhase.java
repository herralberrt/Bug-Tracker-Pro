package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;

public final class StartTestingPhase implements Command {

    private final ObjectNode node;

    /**
     * Constructs a StartTestingPhase command
     */
    public StartTestingPhase(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the start testing phase command
     */
    @Override
    public void execute() {
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        if (AppState.actMiles()) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();

            error.put("command", "startTestingPhase");
            error.put("username", username);
            error.put("timestamp", timestamp);
            error.put("error", "Cannot start a new testing phase.");
            App.addOutput(error);
            return;
        }
        AppState.startTestingPhase(timestamp);
    }

    /**
     * Undoes the start testing phase command
     */
    @Override
    public void undo() {

    }
}
