package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.TicketStatus;
import main.ticket.Ticket;

public class UndoAssignTicket implements Command {

    private final ObjectNode node;
    private final ObjectMapper mapper = new ObjectMapper();

    public UndoAssignTicket(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        String username = node.get("username").asText();
        int ticketID = node.get("ticketID").asInt();
        String timestamp = node.get("timestamp").asText();

        Ticket ticket = AppState.getTicketById(ticketID);
        if (ticket == null) {
            return;
        }

        ObjectNode error = mapper.createObjectNode();
        error.put("command", "undoAssign");
        error.put("username", username);
        error.put("timestamp", timestamp);

        if (ticket.getStatusEnum() != TicketStatus.IN_PROGRESS) {
            error.put("error", "Only IN_PROGRESS tickets can be unassigned.");
            App.addOutput(error);
            return;
        }

        ticket.setAssignedTo("");
        ticket.setAssignedAt(null);
        ticket.setStatus(TicketStatus.OPEN);
        ObjectNode historyEntry = mapper.createObjectNode();
        historyEntry.put("action", "DE-ASSIGNED");
        historyEntry.put("by", username);
        historyEntry.put("timestamp", timestamp);
        ticket.addHistoryEntry(historyEntry);
    }

    @Override
    public void undo() {
    }
}
