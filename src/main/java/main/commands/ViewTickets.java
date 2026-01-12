package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.BusinessPriority;
import main.milestone.Milestone;
import main.ticket.Ticket;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ViewTickets implements Command {

    private final ObjectNode node;
    private final ObjectMapper mapper = new ObjectMapper();

    public ViewTickets(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {

        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();
        LocalDate currentDate = LocalDate.parse(timestamp);

        ObjectNode out = mapper.createObjectNode();
        out.put("command", "viewTickets");
        out.put("username", username);
        out.put("timestamp", timestamp);

        List<Ticket> filtered = AppState.getTickets();

        if (AppState.isReporter(username)) {
            filtered = filtered.stream()
                    .filter(t -> !t.getReportedBy().isEmpty())
                    .filter(t -> t.getReportedBy().equals(username))
                    .collect(Collectors.toList());
        } else if (App.getUserRole(username).equals("DEVELOPER")) {
            filtered = filtered.stream()
                    .filter(t -> {
                        String milestoneName = AppState.getMilestoneNameByTicket(t.getId());
                        if (milestoneName == null) {
                            return false;
                        }
                        Milestone milestone = AppState.getMilestoneByName(milestoneName);
                        if (milestone == null) {
                            return false;
                        }
                        return milestone.getAssignedDevs().contains(username)
                                && t.getStatus().equals("OPEN");
                    })
                    .collect(Collectors.toList());
        }

        filtered = filtered.stream()
                .sorted(Comparator
                        .comparing(Ticket::getCreatedAt)
                        .thenComparing(Ticket::getId))
                .collect(Collectors.toList());

        ArrayNode ticketsArray = mapper.createArrayNode();
        for (Ticket t : filtered) {
            ObjectNode ticketJson = t.toViewJson(mapper);

            String milestoneName = AppState.getMilestoneNameByTicket(t.getId());
            if (milestoneName != null) {
                Milestone milestone = AppState.getMilestoneByName(milestoneName);
                if (milestone != null && !milestone.isBlocked()) {
                    BusinessPriority escalated = determineEscalatedPriorityLevel(
                            ticketJson.get("businessPriority").asText(),
                            milestone,
                            currentDate
                    );
                    ticketJson.put("businessPriority", escalated.name());
                }
            }

            ticketsArray.add(ticketJson);
        }

        out.set("tickets", ticketsArray);
        App.addOutput(out);
    }

    private BusinessPriority determineEscalatedPriorityLevel(String currentPriority,
            Milestone milestone, LocalDate currentDate) {

        BusinessPriority priority = BusinessPriority.valueOf(currentPriority);
        long daysSinceMilestoneCreation = ChronoUnit.DAYS.between(
                milestone.getCreatedAt(), currentDate);

        long daysUntilDue = ChronoUnit.DAYS.between(currentDate, milestone.getDueDate());
        if (daysUntilDue == 1) {
            return BusinessPriority.CRITICAL;
        }

        if (daysSinceMilestoneCreation >= 3) {
            return switch (priority) {
                case LOW -> BusinessPriority.MEDIUM;
                case MEDIUM -> BusinessPriority.HIGH;
                case HIGH -> BusinessPriority.CRITICAL;
                case CRITICAL -> BusinessPriority.CRITICAL;
            };
        }
        return priority;
    }

    @Override
    public void undo() {

    }
}
