package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.ticket.Ticket;

public class AddComment implements Command {

    private final ObjectNode node;

    public AddComment(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        String username = node.get("username").asText();
        int ticketId = node.get("ticketID").asInt();
        String comment = node.get("comment").asText();
        String timestamp = node.get("timestamp").asText();
        Ticket ticket = AppState.getTicketById(ticketId);
        if (ticket == null) {
            return;
        }

        if (ticket.isAnonymous()) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "addComment");
            error.put("username", username);
            error.put("timestamp", timestamp);
            error.put("error", "Comments are not allowed on anonymous tickets.");
            App.addOutput(error);
            return;
        }

        String userRole = App.getUserRole(username);

        if (userRole.equals("REPORTER") && ticket.getStatus().equals("CLOSED")) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "addComment");
            error.put("username", username);
            error.put("timestamp", timestamp);
            error.put("error", "Reporters cannot comment on CLOSED tickets.");
            App.addOutput(error);
            return;
        }

        if (comment.length() < 10) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "addComment");
            error.put("username", username);
            error.put("timestamp", timestamp);
            error.put("error", "Comment must be at least 10 characters long.");
            App.addOutput(error);
            return;
        }

        if (userRole.equals("DEVELOPER")) {
            String assignedTo = ticket.getAssignedTo();
            if (assignedTo == null || !assignedTo.equals(username)) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode error = mapper.createObjectNode();
                error.put("command", "addComment");
                error.put("username", username);
                error.put("timestamp", timestamp);
                error.put("error", "Ticket " + ticketId + " is not assigned to the developer " + username + ".");
                App.addOutput(error);
                return;
            }
        }

        if (userRole.equals("REPORTER")) {
            if (!ticket.getReportedBy().equals(username)) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode error = mapper.createObjectNode();
                error.put("command", "addComment");
                error.put("username", username);
                error.put("timestamp", timestamp);
                error.put("error", "Reporter " + username + " cannot comment on ticket " + ticketId + ".");
                App.addOutput(error);
                return;
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commentNode = mapper.createObjectNode();
        commentNode.put("author", username);
        commentNode.put("content", comment);
        commentNode.put("createdAt", timestamp);

        ticket.addComment(commentNode);
    }

    @Override
    public void undo() {
    }
}
