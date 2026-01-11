package main.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.milestone.Milestone;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

public class CreateMilestone implements Command {

    private final ObjectNode node;

    public CreateMilestone(ObjectNode node) {
        this.node = node;
    }

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
            error.put(
                    "error",
                    "The user does not have permission to execute this command: required role MANAGER; user role " +
                            App.getUserRole(username) + "."
            );
            App.addOutput(error);
            return;
        }

        LocalDate dueDate = LocalDate.parse(node.get("dueDate").asText());
        LocalDate createdAt = LocalDate.parse(timestamp);

        List<String> blockingFor = new ArrayList<>();
        if (node.has("blockingFor")) {
            for (JsonNode n : node.get("blockingFor")) {
                blockingFor.add(n.asText());
            }
        }

        List<Integer> tickets = new ArrayList<>();
        if (node.has("tickets")) {
            for (JsonNode n : node.get("tickets")) {
                tickets.add(n.asInt());
            }
        }

        for (Integer ticketId : tickets) {
            if (AppState.isTicketInMilestone(ticketId)) {
                String milestoneName = AppState.getMilestoneNameByTicket(ticketId);
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode error = mapper.createObjectNode();
                error.put("command", "createMilestone");
                error.put("username", username);
                error.put("timestamp", timestamp);
                error.put(
                        "error",
                        "Tickets " + ticketId + " already assigned to milestone " +
                                milestoneName + "."
                );
                App.addOutput(error);
                return;
            }
        }

        List<String> assignedDevs = new ArrayList<>();
        if (node.has("assignedDevs")) {
            for (JsonNode n : node.get("assignedDevs")) {
                assignedDevs.add(n.asText());
            }
        }

        Milestone existingMilestone = AppState.getMilestoneByName(name);
        if (existingMilestone != null) {
            AppState.removeMilestone(existingMilestone);
        }

        Milestone milestone = new Milestone(name, blockingFor, dueDate, createdAt, tickets, assignedDevs, username);

        AppState.addMilestone(milestone);

        String message = "New milestone " + name +
                " has been created with due date " + dueDate + ".";
        for (String dev : assignedDevs) {
            App.addNotification(dev, timestamp, message);
        }
    }

    @Override
    public void undo() {
    }
}
