package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.BusinessPriority;
import main.ticket.Ticket;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ViewAssignedTickets implements Command {

    private final ObjectNode node;
    private final ObjectMapper mapper = new ObjectMapper();

    public ViewAssignedTickets(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        ObjectNode out = mapper.createObjectNode();
        out.put("command", "viewAssignedTickets");
        out.put("username", username);
        out.put("timestamp", timestamp);

        List<Ticket> assignedTickets = AppState.getTickets()
                .stream()
                .filter(t -> username.equals(t.getAssignedTo()))
                .sorted(
                        Comparator
                                .comparing(
                                        (Ticket t) -> calculatePriorityNumericalValue(t.getBusinessPriority()),
                                        Comparator.reverseOrder())
                                .thenComparing(Ticket::getCreatedAt)
                                .thenComparing(Ticket::getId))
                .collect(Collectors.toList());

        ArrayNode ticketsArray = mapper.createArrayNode();
        for (Ticket t : assignedTickets) {
            ticketsArray.add(t.toJson(mapper));
        }

        out.set("assignedTickets", ticketsArray);
        App.addOutput(out);
    }

    private int calculatePriorityNumericalValue(BusinessPriority priority) {
        return switch (priority) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    @Override
    public void undo() {
    }
}
