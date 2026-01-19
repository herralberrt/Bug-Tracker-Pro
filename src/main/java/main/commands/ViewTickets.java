package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.stream.Collectors;
import main.App;
import main.AppState;
import main.milestone.Milestone;
import main.ticket.Ticket;
import main.enums.BusinessPriority;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.Comparator;


public final class ViewTickets implements Command {
    private static final long NEXT_LEVEL = 3;

    private final ObjectNode node;
    private final ObjectMapper mapper = new ObjectMapper();

    public ViewTickets(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the viewTickets command for the specified user
     */
    @Override
    public void execute() {

        final String username = node.get("username").asText();
        final String timestamp = node.get("timestamp").asText();
        final LocalDate currDate = LocalDate.parse(timestamp);

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
                .sorted(Comparator.comparing(Ticket::getCreatedAt)
                        .thenComparing(Ticket::getId))
                .collect(Collectors.toList());

        ArrayNode arrTicket = mapper.createArrayNode();
        for (Ticket tick : filtered) {
            ObjectNode tickJson = tick.toViewJson(mapper);
            String milestoneName = AppState.getMilestoneNameByTicket(tick.getId());

            if (milestoneName != null) {
                Milestone milestone = AppState.getMilestoneByName(milestoneName);

                if (milestone != null && !milestone.isBlocked()) {
                    BusinessPriority escalated = nextLevel(
                            tickJson.get("businessPriority").asText(),
                            milestone, currDate);
                    tickJson.put("businessPriority", escalated.name());
                }
            }
            arrTicket.add(tickJson);
        }

        out.set("tickets", arrTicket);
        App.addOutput(out);
    }

    /**
     * Calculates the next business priority level based on milestone and date
     */
    private BusinessPriority nextLevel(
            final String currentPriority, final Milestone milestone,
            final LocalDate currDate) {
        BusinessPriority pr = BusinessPriority.valueOf(currentPriority);
        long daysMile = ChronoUnit.DAYS.between(milestone.getCreatedAt(),
                currDate);

        long remDays = ChronoUnit.DAYS.between(currDate, milestone.getDueDate());
        if (remDays == 1) {
            return BusinessPriority.CRITICAL;
        }

        if (daysMile >= NEXT_LEVEL) {
            return switch (pr) {
                case LOW -> BusinessPriority.MEDIUM;
                case MEDIUM -> BusinessPriority.HIGH;
                case HIGH -> BusinessPriority.CRITICAL;
                case CRITICAL -> BusinessPriority.CRITICAL;
            };
        }
        return pr;
    }

    /**
     * Undoes the viewTickets command
     */
    @Override
    public void undo() {

    }
}
