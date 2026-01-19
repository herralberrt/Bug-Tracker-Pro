package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import java.util.List;
import main.enums.BusinessPriority;
import main.ticket.Ticket;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class ViewAssignedTickets implements Command {

    private final ObjectNode node;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs a ViewAssignedTickets command
     */
    public ViewAssignedTickets(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the viewAssignedTickets command
     */
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
                .sorted(Comparator.comparing((Ticket t) -> {
                    BusinessPriority escPriority =
                            AppState.calcPriorityEsc(t, timestamp);
                                            return typePriority(escPriority);
                                            },
                                        Comparator.reverseOrder())
                                .thenComparing(Ticket::getCreatedAt)
                                .thenComparing(Ticket::getId))
                .collect(Collectors.toList());

        ArrayNode arrTick = mapper.createArrayNode();

        for (Ticket tick : assignedTickets) {

            BusinessPriority nextLevel =
                    AppState.calcPriorityEsc(tick, timestamp);
            ObjectNode ticketJson = tick.toJson(mapper);

            ticketJson.put("businessPriority", nextLevel.toString());
            arrTick.add(ticketJson);
        }
        out.set("assignedTickets", arrTick);
        App.addOutput(out);
    }

    private static final int PRIORITY_LOW = 1;
    private static final int PRIORITY_MEDIUM = 2;
    private static final int PRIORITY_HIGH = 3;
    private static final int PRIORITY_CRITICAL = 4;

    /**
     * Returns the value for a business priority
     */
    private int typePriority(final BusinessPriority priority) {
        return switch (priority) {
            case LOW ->
                    PRIORITY_LOW;

            case MEDIUM ->
                    PRIORITY_MEDIUM;

            case HIGH ->
                    PRIORITY_HIGH;

            case CRITICAL ->
                    PRIORITY_CRITICAL;
        };
    }

    /**
     * Undoes the viewAssignedTickets command
     */
    @Override
    public void undo() {

    }
}
