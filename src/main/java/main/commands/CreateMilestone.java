package main.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import java.util.ArrayList;
import java.util.List;
import main.milestone.Milestone;
import java.time.LocalDate;


public final class CreateMilestone implements Command {

    private final ObjectNode node;

    /**
     * Constructs a CreateMilestone command
     */
    public CreateMilestone(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the create milestone command
     */
    @Override
    public void execute() {
        String username = node.get("username").asText();
        String name = node.get("name").asText();
        String timestamp = node.get("timestamp").asText();

        if (!AppState.isManager(username)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();

            error.put("command", "createMilestone");
            error.put("username", username);
            error.put("timestamp", timestamp);
            error.put("error",
                    "The user does not have permission to execute this command:"
                            + " required role MANAGER; user role "
                            + App.getUserRole(username) + ".");
            App.addOutput(error);
            return;
        }

        LocalDate dueDate = LocalDate.parse(node.get("dueDate").asText());
        LocalDate atDate = LocalDate.parse(timestamp);
        List<String> blockingFor = new ArrayList<>();

        if (node.has("blockingFor")) {
            for (JsonNode nod : node.get("blockingFor")) {
                blockingFor.add(nod.asText());
            }
        }

        List<Integer> tickets = new ArrayList<>();
        if (node.has("tickets")) {
            for (JsonNode nod : node.get("tickets")) {
                tickets.add(nod.asInt());
            }
        }

        for (Integer ticketId : tickets) {
            if (AppState.isTicketInMilestone(ticketId)) {

                String numeMiles = AppState.getMilestoneNameByTicket(ticketId);
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode error = mapper.createObjectNode();

                error.put("command", "createMilestone");
                error.put("username", username);
                error.put("timestamp", timestamp);
                error.put("error",
                        "Tickets " + ticketId + " already assigned to milestone "
                                + numeMiles + ".");
                App.addOutput(error);
                return;
            }
        }

        List<String> assignedDevs = new ArrayList<>();
        if (node.has("assignedDevs")) {
            for (JsonNode nod : node.get("assignedDevs")) {
                assignedDevs.add(nod.asText());
            }
        }

        Milestone exsisMiles = AppState.getMilestoneByName(name);
        if (exsisMiles != null) {
            AppState.removeMilestone(exsisMiles);
        }

        Milestone milestone = new Milestone(name, blockingFor, dueDate,
                atDate, tickets, assignedDevs, username);

        AppState.addMilestone(milestone);
        ObjectMapper mapper = new ObjectMapper();
        for (Integer ticketId : tickets) {
            main.ticket.Ticket ticket = AppState.getTicketById(ticketId);
            if (ticket != null) {
                ObjectNode historyEntry = mapper.createObjectNode();
                historyEntry.put("action", "ADDED_TO_MILESTONE");
                historyEntry.put("milestone", name);
                historyEntry.put("by", username);
                historyEntry.put("timestamp", timestamp);
                ticket.addHistoryEntry(historyEntry);
            }
        }

        main.notif.Notification notif
                = main.notif.NotificationFactory.createMilestone(name, dueDate);
        for (String dev : assignedDevs) {
            App.addNotification(dev, timestamp, notif.getMessage());
        }
    }

    /**
     * Undoes the create milestone command
     */
    @Override
    public void undo() {
    }
}
