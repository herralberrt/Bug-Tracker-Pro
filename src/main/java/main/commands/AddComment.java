package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.ticket.Ticket;

public final class AddComment implements Command {
    private static final String COMMAND_NAME = "addComment";
    private static final int MIN_COMMENT_LENGTH = 10;
    private final ObjectNode input;

    /**
     * Constructs an AddComment command
     */
    public AddComment(final ObjectNode input) {
        this.input = input;
    }

    /**
     * Executes the add comment command
     */
    @Override
    public void execute() {
        String username = input.get("username").asText();
        int ticketId = input.get("ticketID").asInt();
        String commentText = input.get("comment").asText();
        String timestamp = input.get("timestamp").asText();
        Ticket ticket = AppState.getTicketById(ticketId);

        if (ticket == null) {
            return;
        }

        String rol = App.getUserRole(username);

        if (ticket.isAnonymous()) {
            addError(username, timestamp, "Comments are not allowed on anonymous tickets.");
            return;
        }

        if (rol.equals("REPORTER") && ticket.getStatus().equals("CLOSED")) {
            addError(username, timestamp, "Reporters cannot comment on CLOSED tickets.");
            return;
        }

        if (commentText.length() < MIN_COMMENT_LENGTH) {
            addError(username, timestamp, "Comment must be at least 10 characters long.");
            return;
        }

        if (rol.equals("DEVELOPER")
                && !username.equals(ticket.getAssignedTo())) {
            addError(username, timestamp,
                    "Ticket " + ticketId + " is not assigned to the developer " + username + ".");
            return;
        }

        if (rol.equals("REPORTER")
                && !username.equals(ticket.getReportedBy())) {
            addError(username, timestamp,
                    "Reporter " + username + " cannot comment on ticket " + ticketId + ".");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commentNode = mapper.createObjectNode();

        commentNode.put("author", username);
        commentNode.put("content", commentText);
        commentNode.put("createdAt", timestamp);
        ticket.addComment(commentNode);
    }

    /**
     * Adds an error message to the output for the add comment command
     */
    private void addError(final String username,
                          final String timestamp,
                          final String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode error = mapper.createObjectNode();

        error.put("command", COMMAND_NAME);
        error.put("username", username);
        error.put("timestamp", timestamp);
        error.put("error", message);
        App.addOutput(error);
    }

    /**
     * Undoes the add comment command
     */
    @Override
    public void undo() {

    }
}
