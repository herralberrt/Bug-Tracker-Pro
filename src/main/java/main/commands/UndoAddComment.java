package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.ticket.Ticket;

public class UndoAddComment implements Command {

    private final ObjectNode node;

    public UndoAddComment(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        String username = node.get("username").asText();
        int ticketId = node.get("ticketID").asInt();
        String timestamp = node.get("timestamp").asText();
        Ticket ticket = AppState.getTicketById(ticketId);
        if (ticket == null) {
            return;
        }

        if (ticket.isAnonymous()) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "undoAddComment");
            error.put("username", username);
            error.put("timestamp", timestamp);
            error.put("error", "Comments are not allowed on anonymous tickets.");
            App.addOutput(error);
            return;
        }
        ticket.removeLastCommentByUser(username);
    }

    @Override
    public void undo() {
    }
}
