package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import main.ticket.Ticket;
import main.milestone.Milestone;
import main.App;
import main.AppState;
import java.util.Comparator;

public final class ViewTicketHistory implements Command {

    private final ObjectNode node;

    /**
     * Constructs a ViewTicketHistory command
     */
    public ViewTicketHistory(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the viewTicketHistory command
     */
    @Override
    public void execute() {
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", "viewTicketHistory");
        output.put("username", username);
        output.put("timestamp", timestamp);

        if (!AppState.isDeveloper(username) && !AppState.isManager(username)) {
            App.addOutput(output);
            return;
        }

        List<Ticket> vizTick = new ArrayList<>();

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
                    vizTick.add(ticket);
                }
            }
        } else if (AppState.isManager(username)) {
            for (Ticket tick : AppState.getAllTickets()) {
                String milestoneName = AppState.getMilestoneNameByTicket(tick.getId());
                if (milestoneName != null) {
                    Milestone milestone = AppState.getMilestoneByName(milestoneName);
                    if (milestone != null && milestone.getCreatedBy().equals(username)) {
                        vizTick.add(tick);
                    }
                }
            }
        }

        vizTick.sort(Comparator.comparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId));

        ArrayNode arrTick = mapper.createArrayNode();
        for (Ticket ticket : vizTick) {
            ObjectNode ticketEntry = mapper.createObjectNode();
            ticketEntry.put("id", ticket.getId());
            ticketEntry.put("title", ticket.getTitle());
            ticketEntry.put("status", ticket.getStatus());
            ArrayNode arrAct = mapper.createArrayNode();
            boolean currOk = false;

            for (ObjectNode historyEntry : ticket.getHistory()) {
                String act = historyEntry.get("action").asText();

                ObjectNode orderedEntry = ordEntry(historyEntry, mapper);

                if (AppState.isDeveloper(username)) {
                    if (act.equals("ADDED_TO_MILESTONE")) {
                        arrAct.add(orderedEntry);
                        continue;
                    }

                    if (act.equals("ASSIGNED")
                            && historyEntry.get("by").asText().equals(username)) {
                        currOk = true;
                    }

                    if (currOk) {
                        arrAct.add(orderedEntry);
                    }

                    if (act.equals("DE-ASSIGNED")
                            && historyEntry.get("by").asText().equals(username)) {
                        currOk = false;
                    }
                } else {
                    arrAct.add(orderedEntry);
                }
            }
            ticketEntry.set("actions", arrAct);
            ArrayNode arrComm = mapper.createArrayNode();
            for (ObjectNode comment : ticket.getComments()) {
                arrComm.add(comment);
            }
            ticketEntry.set("comments", arrComm);
            arrTick.add(ticketEntry);
        }
        output.set("ticketHistory", arrTick);
        App.addOutput(output);
    }

    /**
     * Builds an ordered entry for a ticket history action
     */
    private static ObjectNode ordEntry(final ObjectNode entry,
                                       final ObjectMapper mapper) {
        String act = entry.get("action").asText();
        ObjectNode ordered = mapper.createObjectNode();

        ordered.put("action", act);

        switch (act) {
            case "ADDED_TO_MILESTONE" -> {
                if (entry.has("milestone")) {
                    ordered.set("milestone", entry.get("milestone"));
                }

                if (entry.has("by")) {
                    ordered.set("by", entry.get("by"));
                }

                if (entry.has("timestamp")) {
                    ordered.set("timestamp", entry.get("timestamp"));
                }
            }

            case "ASSIGNED", "DE-ASSIGNED" -> {
                if (entry.has("by")) {
                    ordered.set("by", entry.get("by"));
                }

                if (entry.has("timestamp")) {
                    ordered.set("timestamp", entry.get("timestamp"));
                }
            }

            case "STATUS_CHANGED" -> {
                if (entry.has("from")) {
                    ordered.set("from", entry.get("from"));
                }

                if (entry.has("to")) {
                    ordered.set("to", entry.get("to"));
                }

                if (entry.has("by")) {
                    ordered.set("by", entry.get("by"));
                }

                if (entry.has("timestamp")) {
                    ordered.set("timestamp", entry.get("timestamp"));
                }

            }

            default -> {
                entry.fieldNames().forEachRemaining(key -> {
                    if (!key.equals("action") && !ordered.has(key)) {
                        ordered.set(key, entry.get(key));
                    }
                });
            }
        }
        return ordered;
    }

    /**
     * Undoes the viewTicketHistory command
     */
    @Override
    public void undo() {
    }
}
