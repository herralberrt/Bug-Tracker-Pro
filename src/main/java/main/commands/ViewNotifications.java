package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;

import java.util.List;

public class ViewNotifications implements Command {

    private final ObjectNode node;

    public ViewNotifications(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        ObjectNode result = mapper.createObjectNode();
        result.put("command", "viewNotifications");
        result.put("username", username);
        result.put("timestamp", timestamp);

        ArrayNode notificationsArray = mapper.createArrayNode();
        List<String> notifications = App.getNotifications(username);
        for (String notification : notifications) {
            notificationsArray.add(notification);
        }

        result.set("notifications", notificationsArray);
        App.addOutput(result);

        App.clearNotifications(username);
    }

    @Override
    public void undo() {
    }
}
