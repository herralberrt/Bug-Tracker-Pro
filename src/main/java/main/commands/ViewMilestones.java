package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.milestone.Milestone;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ViewMilestones implements Command {

    private final ObjectNode node;

    public ViewMilestones(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();
        LocalDate currentDate = LocalDate.parse(timestamp);
        
        String role = App.getUserRole(username);
        
        List<Milestone> relevantMilestones = new ArrayList<>();
        
        if (role.equals("MANAGER")) {
            for (Milestone m : AppState.getMilestones()) {
                if (m.getCreatedBy().equals(username)) {
                    relevantMilestones.add(m);
                }
            }
        } else if (role.equals("DEVELOPER")) {
            for (Milestone m : AppState.getMilestones()) {
                if (m.getAssignedDevs().contains(username)) {
                    relevantMilestones.add(m);
                }
            }
        }
        
        relevantMilestones.sort((m1, m2) -> {
            int dateCmp = m1.getDueDate().compareTo(m2.getDueDate());
            if (dateCmp != 0) {
                return dateCmp;
            }
            return m1.getName().compareTo(m2.getName());
        });
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode milestonesArray = mapper.createArrayNode();
        
        for (Milestone m : relevantMilestones) {
            milestonesArray.add(m.toJson(mapper, currentDate));
        }
        
        ObjectNode output = mapper.createObjectNode();
        output.put("command", "viewMilestones");
        output.put("username", username);
        output.put("timestamp", timestamp);
        output.set("milestones", milestonesArray);
        
        App.addOutput(output);
    }

    @Override
    public void undo() {
    }
}
