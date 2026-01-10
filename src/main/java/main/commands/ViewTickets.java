package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.ticket.Ticket;

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

        ObjectNode out = mapper.createObjectNode();
        out.put("command", "viewTickets");
        out.put("username", username);
        out.put("timestamp", node.get("timestamp").asText());

        List<Ticket> filtered = AppState.getTickets();

        if (AppState.isReporter(username)) {
            filtered = filtered.stream()
                    .filter(t -> !t.getReportedBy().isEmpty())
                    .filter(t -> t.getReportedBy().equals(username))
                    .collect(Collectors.toList());
        }

        filtered = filtered.stream()
                .sorted(Comparator
                        .comparing(Ticket::getCreatedAt)
                        .thenComparing(Ticket::getId))
                .collect(Collectors.toList());

        ArrayNode ticketsArray = mapper.createArrayNode();
        for (Ticket t : filtered) {
            ticketsArray.add(t.toJson(mapper));
        }

        out.set("tickets", ticketsArray);
        App.addOutput(out);
    }

    @Override
    public void undo() {}
}
