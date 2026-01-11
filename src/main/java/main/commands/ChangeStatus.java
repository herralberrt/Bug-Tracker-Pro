package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.TicketStatus;
import main.ticket.Ticket;
import java.time.LocalDate;

public class ChangeStatus implements Command {

    private final ObjectNode node;
    private final String username;
    private final int ticketID;
    private final LocalDate timestamp;

    public ChangeStatus(ObjectNode node) {
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
            error.put("command", "changeStatus");
            error.put("username", username);
            error.put("timestamp", timestamp.toString());
            error.put("error", "Ticket " + ticketID + " is not assigned to developer " + username + ".");
            App.addOutput(error);
            return;
        }

        if (ticket.getStatusEnum() == TicketStatus.CLOSED) {
            return;
        }

        TicketStatus oldStatus = ticket.getStatusEnum();
        TicketStatus newStatus = null;

        if (oldStatus == TicketStatus.IN_PROGRESS) {
            newStatus = TicketStatus.RESOLVED;
            ticket.setSolvedAt(timestamp);
        } else if (oldStatus == TicketStatus.RESOLVED) {
            newStatus = TicketStatus.CLOSED;
        }

        if (newStatus != null) {
            ticket.setStatus(newStatus);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode historyEntry = mapper.createObjectNode();
            historyEntry.put("action", "STATUS_CHANGED");
            historyEntry.put("from", oldStatus.name());
            historyEntry.put("to", newStatus.name());
            historyEntry.put("by", username);
            historyEntry.put("timestamp", timestamp.toString());
            ticket.addHistoryEntry(historyEntry);

           if (newStatus == TicketStatus.CLOSED) {
                String milestoneName = AppState.getMilestoneNameByTicket(ticketID);
                if (milestoneName != null) {
                    main.milestone.Milestone milestone = AppState.getMilestoneByName(milestoneName);
                    if (milestone != null) {
                        milestone.freezeMetricsIfCompleted(timestamp);
                        AppState.checkMilestoneUnblocking(milestoneName, ticketID, timestamp.toString());
                    }
                }
            }
        }
    }

    @Override
    public void undo() {
    }
}
