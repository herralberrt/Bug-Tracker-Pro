package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.BusinessPriority;
import main.enums.ExpertiseArea;
import main.enums.Seniority;
import main.enums.TicketStatus;
import main.enums.Type;
import main.milestone.Milestone;
import main.ticket.Ticket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssignTicket implements Command {

    private final ObjectNode node;
    private final ObjectMapper mapper = new ObjectMapper();

    public AssignTicket(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        String username = node.get("username").asText();
        int ticketID = node.get("ticketID").asInt();
        String timestamp = node.get("timestamp").asText();
        LocalDate currentDate = LocalDate.parse(timestamp);

        Ticket ticket = AppState.getTicketById(ticketID);
        if (ticket == null) {
            return;
        }

        ObjectNode developer = AppState.getDeveloperByUsername(username);
        if (developer == null) {
            return;
        }

        ExpertiseArea devExpertiseArea = ExpertiseArea.valueOf(
                developer.get("expertiseArea").asText()
        );
        Seniority devSeniority = Seniority.valueOf(
                developer.get("seniority").asText()
        );

        ObjectNode error = mapper.createObjectNode();
        error.put("command", "assignTicket");
        error.put("username", username);
        error.put("timestamp", timestamp);

        if (!validateDeveloperExpertiseAreaAccess(devExpertiseArea, ticket.getExpertiseArea())) {
            List<String> requiredAreas = buildRequiredExpertiseAreasList(
                    ticket.getExpertiseArea()
            );
            error.put(
                    "error",
                    String.format(
                            "Developer %s cannot assign ticket %d due to expertise area. Required: %s; Current: %s.",
                            username,
                            ticketID,
                            String.join(", ", requiredAreas),
                            devExpertiseArea.name()
                    )
            );
            App.addOutput(error);
            return;
        }

        if (!validateDeveloperSeniorityLevelAccess(devSeniority, ticket.getBusinessPriority(), ticket.getType())) {
            List<String> requiredLevels = buildRequiredSeniorityLevelsList(
                    ticket.getBusinessPriority(),
                    ticket.getType()
            );
            error.put(
                    "error",
                    String.format(
                            "Developer %s cannot assign ticket %d due to seniority level. Required: %s; Current: %s.",
                            username,
                            ticketID,
                            String.join(", ", requiredLevels),
                            devSeniority.name()
                    )
            );
            App.addOutput(error);
            return;
        }

        if (ticket.getStatusEnum() != TicketStatus.OPEN) {
            error.put("error", "Only OPEN tickets can be assigned.");
            App.addOutput(error);
            return;
        }

        String milestoneName = AppState.getMilestoneNameByTicket(ticketID);
        if (milestoneName != null) {
            Milestone milestone = AppState.getMilestoneByName(milestoneName);
            if (milestone != null) {
                if (!milestone.getAssignedDevs().contains(username)) {
                    error.put(
                            "error",
                            String.format(
                                    "Developer %s is not assigned to milestone %s.",
                                    username,
                                    milestoneName
                            )
                    );
                    App.addOutput(error);
                    return;
                }

                if (milestone.isBlocked()) {
                    error.put(
                            "error",
                            String.format(
                                    "Cannot assign ticket %d from blocked milestone %s.",
                                    ticketID,
                                    milestoneName));
                    App.addOutput(error);
                    return;
                }
            }
        }

        ticket.setAssignedTo(username);
        ticket.setAssignedAt(currentDate);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        ObjectNode assignedEntry = mapper.createObjectNode();
        assignedEntry.put("action", "ASSIGNED");
        assignedEntry.put("by", username);
        assignedEntry.put("timestamp", timestamp);
        ticket.addHistoryEntry(assignedEntry);

        ObjectNode statusEntry = mapper.createObjectNode();
        statusEntry.put("action", "STATUS_CHANGED");
        statusEntry.put("from", "OPEN");
        statusEntry.put("to", "IN_PROGRESS");
        statusEntry.put("by", username);
        statusEntry.put("timestamp", timestamp);
        ticket.addHistoryEntry(statusEntry);
    }

    private boolean validateDeveloperExpertiseAreaAccess(
            ExpertiseArea devArea,
            ExpertiseArea ticketArea
    ) {
        return switch (devArea) {
            case FULLSTACK -> true;
            case FRONTEND ->
                    ticketArea == ExpertiseArea.FRONTEND ||
                            ticketArea == ExpertiseArea.DESIGN;
            case BACKEND ->
                    ticketArea == ExpertiseArea.BACKEND ||
                            ticketArea == ExpertiseArea.DB;
            case DESIGN ->
                    ticketArea == ExpertiseArea.DESIGN ||
                            ticketArea == ExpertiseArea.FRONTEND;
            case DB -> ticketArea == ExpertiseArea.DB;
            case DEVOPS -> ticketArea == ExpertiseArea.DEVOPS;
        };
    }

    private List<String> buildRequiredExpertiseAreasList(ExpertiseArea ticketArea) {
        List<String> areas = new ArrayList<>();
        switch (ticketArea) {
            case FRONTEND:
                areas.addAll(Arrays.asList("DESIGN", "FRONTEND", "FULLSTACK"));
                break;
            case BACKEND:
                areas.addAll(Arrays.asList("BACKEND", "FULLSTACK"));
                break;
            case DEVOPS:
                areas.addAll(Arrays.asList("DEVOPS", "FULLSTACK"));
                break;
            case DESIGN:
                areas.addAll(Arrays.asList("DESIGN", "FRONTEND", "FULLSTACK"));
                break;
            case DB:
                areas.addAll(Arrays.asList("BACKEND", "DB", "FULLSTACK"));
                break;
        }
        areas.sort(String::compareTo);
        return areas;
    }

    private boolean validateDeveloperSeniorityLevelAccess(
            Seniority seniority, BusinessPriority priority, Type type) {
        if (seniority == Seniority.SENIOR) {
            return true;
        }

        if (seniority == Seniority.JUNIOR) {
            if (type == Type.FEATURE_REQUEST) {
                return false;
            }
            return priority == BusinessPriority.LOW ||
                    priority == BusinessPriority.MEDIUM;
        }

        if (seniority == Seniority.MID) {
            if (priority == BusinessPriority.CRITICAL) {
                return false;
            }
            return true;
        }

        return false;
    }

    private List<String> buildRequiredSeniorityLevelsList(
            BusinessPriority priority,
            Type type
    ) {
        List<String> levels = new ArrayList<>();

        if (priority == BusinessPriority.CRITICAL || type == Type.FEATURE_REQUEST) {
            if (priority == BusinessPriority.CRITICAL) {
                levels.add("SENIOR");
            } else if (type == Type.FEATURE_REQUEST) {
                levels.add("MID");
                levels.add("SENIOR");
            }
        } else if (priority == BusinessPriority.HIGH) {
            levels.add("MID");
            levels.add("SENIOR");
        } else {
            levels.add("JUNIOR");
            levels.add("MID");
            levels.add("SENIOR");
        }

        levels.sort(String::compareTo);
        return levels;
    }

    @Override
    public void undo() {
    }
}
