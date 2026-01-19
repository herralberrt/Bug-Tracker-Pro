package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.TicketStatus;
import main.ticket.Ticket;

import java.time.LocalDate;

public final class ChangeStatus implements Command {

    private final ObjectNode node;
    private final String username;
    private final int ticketID;
    private final LocalDate timestamp;

    /**
     * Constructs a ChangeStatus command
     */
    public ChangeStatus(final ObjectNode node) {
        this.node = node;
        this.username = node.get("username").asText();
        this.ticketID = node.get("ticketID").asInt();
        this.timestamp = LocalDate.parse(node.get("timestamp").asText());
    }

    /**
     * Executes the change status command
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
            
            error.put("command", "changeStatus");
            error.put("username", username);
            error.put("timestamp", timestamp.toString());
            error.put("error", "Ticket " + ticketID
                    + " is not assigned to developer " + username + ".");
            App.addOutput(error);
            return;
        }

        if (ticket.getStatusEnum() == TicketStatus.CLOSED) {
            return;
        }

        TicketStatus oldStat = ticket.getStatusEnum();
        TicketStatus newStat = null;

        if (oldStat == TicketStatus.IN_PROGRESS) {
            newStat = TicketStatus.RESOLVED;
            ticket.setSolvedAt(timestamp);
        } else if (oldStat == TicketStatus.RESOLVED) {
            newStat = TicketStatus.CLOSED;
        }

        if (newStat != null) {
            ticket.setStatus(newStat);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode historyEntry = mapper.createObjectNode();

            historyEntry.put("from", oldStat.name());
            historyEntry.put("to", newStat.name());
            historyEntry.put("by", username);
            historyEntry.put("timestamp", timestamp.toString());
            historyEntry.put("action", "STATUS_CHANGED");
            ticket.addHistoryEntry(historyEntry);

            if (newStat == TicketStatus.CLOSED) {
                String milestoneName = AppState.getMilestoneNameByTicket(ticketID);
                if (milestoneName != null) {
                    main.milestone.Milestone milestone = AppState.getMilestoneByName(milestoneName);
                    if (milestone != null) {
                        milestone.freezeMetricsIfCompleted(timestamp);
                        AppState.milesUnblock(milestoneName, ticketID, timestamp.toString());
                    }
                }
            }
        }
    }

    /**
     * Undoes the change status command
     */
    @Override
    public void undo() {

    }
}
