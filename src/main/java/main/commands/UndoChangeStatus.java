package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import java.time.LocalDate;
import main.enums.TicketStatus;
import main.ticket.Ticket;

public final class UndoChangeStatus implements Command {

    private final ObjectNode node;
    private final String username;
    private final int ticketID;
    private final LocalDate timestamp;

    /**
     * Constructs an UndoChangeStatus command
     */
    public UndoChangeStatus(final ObjectNode node) {
        this.node = node;
        this.username = node.get("username").asText();
        this.ticketID = node.get("ticketID").asInt();
        this.timestamp = LocalDate.parse(node.get("timestamp").asText());
    }

    /**
     * Executes the undo change status command
     */
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
            error.put("error", "Ticket " + ticketID
                    + " is not assigned to developer " + username + ".");
            App.addOutput(error);
            return;
        }

        if (ticket.getStatusEnum() == TicketStatus.IN_PROGRESS) {
            return;
        }

        TicketStatus currStat = ticket.getStatusEnum();
        TicketStatus lastStat = null;

        if (currStat == TicketStatus.RESOLVED) {
            lastStat = TicketStatus.IN_PROGRESS;
            ticket.setSolvedAt(null);
        } else if (currStat == TicketStatus.CLOSED) {
            lastStat = TicketStatus.RESOLVED;
        }

        if (lastStat != null) {
            ticket.setStatus(lastStat);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode historyEntry = mapper.createObjectNode();
            historyEntry.put("action", "STATUS_CHANGED");
            historyEntry.put("from", currStat.name());
            historyEntry.put("to", lastStat.name());
            historyEntry.put("by", username);
            historyEntry.put("timestamp", timestamp.toString());
            ticket.addHistoryEntry(historyEntry);
        }
    }

    /**
     * Undoes the undo change status command
     */
    @Override
    public void undo() {

    }
}
