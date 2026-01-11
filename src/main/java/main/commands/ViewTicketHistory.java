package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.ticket.Ticket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ViewTicketHistory implements Command {

    private final ObjectNode node;

    public ViewTicketHistory(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();
        LocalDate currentDate = LocalDate.parse(timestamp);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();
        output.put("command", "viewTicketHistory");
        output.put("username", username);
        output.put("timestamp", timestamp);

        List<Ticket> visibleTickets = new ArrayList<>();

        if (AppState.isDeveloper(username)) {
            for (Ticket ticket : AppState.getAllTickets()) {
                boolean hasHistory = false;
                for (ObjectNode entry : ticket.getHistory()) {
                    if (entry.has("by") && entry.get("by").asText().equals(username)) {
                        hasHistory = true;
                        break;
                    }
                }
                if (hasHistory) {
                    visibleTickets.add(ticket);
                }
            }
        } else if (AppState.isManager(username)) {
            for (Ticket ticket : AppState.getAllTickets()) {
                String milestoneName = AppState.getMilestoneNameByTicket(ticket.getId());
                if (milestoneName != null) {
                    var milestone = AppState.getMilestoneByName(milestoneName);
                    if (milestone != null && milestone.getCreatedBy().equals(username)) {
                        visibleTickets.add(ticket);
                    }
                }
            }
        }

        visibleTickets.sort(Comparator
                .comparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId));

        ArrayNode ticketHistoryArray = mapper.createArrayNode();
        for (Ticket ticket : visibleTickets) {
            ObjectNode ticketEntry = mapper.createObjectNode();
            ticketEntry.put("id", ticket.getId());
            ticketEntry.put("title", ticket.getTitle());
            ticketEntry.put("status", ticket.getStatus());

            ArrayNode actionsArray = mapper.createArrayNode();
            for (ObjectNode historyEntry : ticket.getHistory()) {
                actionsArray.add(historyEntry);
            }
            ticketEntry.set("actions", actionsArray);

            ArrayNode commentsArray = mapper.createArrayNode();
            for (ObjectNode comment : ticket.getComments()) {
                commentsArray.add(comment);
            }
            ticketEntry.set("comments", commentsArray);

            ticketHistoryArray.add(ticketEntry);
        }

        output.set("ticketHistory", ticketHistoryArray);
        App.addOutput(output);
    }

    @Override
    public void undo() {
    }
}
