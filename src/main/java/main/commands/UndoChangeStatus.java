package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.TicketStatus;
import main.ticket.Ticket;

import java.time.LocalDate;

public class UndoChangeStatus implements Command {

    private final ObjectNode node;
    private final String username;
    private final int ticketID;
    private final LocalDate timestamp;

    public UndoChangeStatus(ObjectNode node) {
        this.node = node;
        this.username = node.get("username").asText();
        this.ticketID = node.get("ticketID").asInt();
        this.timestamp = LocalDate.parse(node.get("timestamp").asText());
    }

    @Override
    public void execute() {
        Ticket ticket = AppState.getTicketById(ticketID);

        if (ticket == null) {
            return;
        }

        if (!username.equals(ticket.getAssignedTo())) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "undoChangeStatus");
            error.put("username", username);
            error.put("timestamp", timestamp.toString());
            error.put("error", "Ticket " + ticketID + " is not assigned to developer " + username + ".");
            App.addOutput(error);
            return;
        }

        if (ticket.getStatusEnum() == TicketStatus.IN_PROGRESS) {
            return;
        }

        TicketStatus currentStatus = ticket.getStatusEnum();
        TicketStatus previousStatus = null;

        if (currentStatus == TicketStatus.RESOLVED) {
            previousStatus = TicketStatus.IN_PROGRESS;
            ticket.setSolvedAt(null);
        } else if (currentStatus == TicketStatus.CLOSED) {
            previousStatus = TicketStatus.RESOLVED;
        }

        if (previousStatus != null) {
            ticket.setStatus(previousStatus);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode historyEntry = mapper.createObjectNode();
            historyEntry.put("action", "STATUS_CHANGED");
            historyEntry.put("from", currentStatus.name());
            historyEntry.put("to", previousStatus.name());
            historyEntry.put("by", username);
            historyEntry.put("timestamp", timestamp.toString());
            ticket.addHistoryEntry(historyEntry);
        }
    }

    @Override
    public void undo() {
    }
}
