package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.ticket.Ticket;
import main.ticket.TicketFactory;
import main.enums.Type;
import main.enums.BusinessPriority;

public class ReportTicket implements Command {

    private final ObjectNode node;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReportTicket(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {

        String username = node.get("username").asText();

        if (AppState.investorsLost()) {
            ObjectNode out = mapper.createObjectNode();
            out.put("command", "reportTicket");
            out.put("username", username);
            out.put("timestamp", node.get("timestamp").asText());
            out.put("error", "Tickets can only be reported during testing phases.");
            App.addOutput(out);
            return;
        }

        if (!AppState.userExists(username)) {
            ObjectNode out = mapper.createObjectNode();
            out.put("command", "reportTicket");
            out.put("username", username);
            out.put("timestamp", node.get("timestamp").asText());
            out.put("error", "The user " + username + " does not exist.");
            App.addOutput(out);
            return;
        }

        ObjectNode params = (ObjectNode) node.get("params");
        String reportedBy = params.get("reportedBy").asText();
        Type type = Type.valueOf(params.get("type").asText());

        if (reportedBy.isEmpty() && type != Type.BUG) {
            ObjectNode out = mapper.createObjectNode();
            out.put("command", "reportTicket");
            out.put("username", username);
            out.put("timestamp", node.get("timestamp").asText());
            out.put("error", "Anonymous reports are only allowed for tickets of type BUG.");
            App.addOutput(out);
            return;
        }

        if (reportedBy.isEmpty()) {
            params.put("businessPriority", BusinessPriority.LOW.name());
        }

        Ticket ticket = TicketFactory.create(
                AppState.nextTicketId(),
                node.get("timestamp").asText(),
                params
        );

        AppState.addTicket(ticket);
    }

    @Override
    public void undo() {}
}
