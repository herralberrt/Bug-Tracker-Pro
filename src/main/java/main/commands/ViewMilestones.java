package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import java.util.ArrayList;
import java.util.List;
import main.milestone.Milestone;
import java.time.LocalDate;
import java.util.Comparator;

public final class ViewMilestones implements Command {

    private final ObjectNode node;

    /**
     * Constructs a ViewMilestones command
     */
    public ViewMilestones(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the viewMilestones command
     */
    @Override
    public void execute() {
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        LocalDate currentDate = LocalDate.parse(timestamp);
        String rol = App.getUserRole(username);
        List<Milestone> utilMilestones = new ArrayList<>();

        if (rol.equals("MANAGER")) {
            for (Milestone miles : AppState.getMilestones()) {
                if (miles.getCreatedBy().equals(username)) {
                    utilMilestones.add(miles);
                }
            }
        } else if (rol.equals("DEVELOPER")) {
            for (Milestone miles : AppState.getMilestones()) {
                if (miles.getAssignedDevs().contains(username)) {
                    utilMilestones.add(miles);
                }
            }
        }

        utilMilestones.sort(Comparator.comparing(Milestone::getDueDate)
                        .thenComparing(Milestone::getName));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrMiles = mapper.createArrayNode();

        for (Milestone miles : utilMilestones) {
            arrMiles.add(miles.toJson(mapper, currentDate));
        }

        ObjectNode output = mapper.createObjectNode();

        output.put("command", "viewMilestones");
        output.put("username", username);
        output.put("timestamp", timestamp);
        output.set("milestones", arrMiles);
        App.addOutput(output);
    }

    /**
     * Undoes the viewMilestones command
     */
    @Override
    public void undo() {
    }
}
