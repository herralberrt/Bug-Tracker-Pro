package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import main.App;


public final class ViewNotifications implements Command {

    private final ObjectNode node;

    /**
     * Constructs a ViewNotifications command
     */
    public ViewNotifications(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the viewNotifications command
     */
    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        ObjectNode result = mapper.createObjectNode();
        result.put("command", "viewNotifications");
        result.put("username", username);
        result.put("timestamp", timestamp);

        ArrayNode arrNot = mapper.createArrayNode();
        main.utiliz.Developer dev = main.AppState.getDeveloperInstanceByUsername(username);
        if (dev != null) {

            List<String> notifications = dev.getNotifications();

            for (String notification : notifications) {
                arrNot.add(notification);
            }
            notifications.clear();
        }
        result.set("notifications", arrNot);
        App.addOutput(result);
    }

    /**
     * Undoes the viewNotifications command
     */
    @Override
    public void undo() {
    }
}
