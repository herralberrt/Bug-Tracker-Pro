package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.BusinessPriority;
import main.enums.ExpertiseArea;
import main.enums.Seniority;
import main.enums.TicketStatus;
import main.milestone.Milestone;
import main.ticket.Ticket;
import main.enums.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;

public final class AssignTicket implements Command {

    private final ObjectNode node;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs an AssignTicket command
     */
    public AssignTicket(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the assign ticket command for the specified user
     */
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

        ObjectNode dev = AppState.getDeveloperByUsername(username);
        if (dev == null) {
            return;
        }
        ExpertiseArea devExpertiseArea =
                ExpertiseArea.valueOf(dev.get("expertiseArea").asText());
        Seniority devSeniority = Seniority.valueOf(dev.get("seniority").asText());

        ObjectNode error = mapper.createObjectNode();
        error.put("command", "assignTicket");
        error.put("username", username);
        error.put("timestamp", timestamp);

        if (!validDev(devExpertiseArea, ticket.getExpertiseArea())) {
            List<String> req = buildReqExp(ticket.getExpertiseArea());
            error.put("error", String.format(
                            "Developer %s cannot assign ticket %d due to expertise area. Required: %s; Current: %s.",
                            username, ticketID, String.join(", ", req),
                            devExpertiseArea.name()));
            App.addOutput(error);
            return;
        }

        if (!valDev(devSeniority, ticket.getBusinessPriority(), ticket.getType())) {
            List<String> requiredniv = reqSenLevels(
                    ticket.getBusinessPriority(), ticket.getType());
            error.put(
                    "error",
                    String.format(
                            "Developer %s cannot assign ticket %d due to seniority level. Required: %s; Current: %s.",
                            username, ticketID, String.join(", ", requiredniv),
                            devSeniority.name()));
            App.addOutput(error);
            return;
        }

        if (ticket.getStatusEnum() != TicketStatus.OPEN) {
            error.put("error", "Only OPEN tickets can be assigned.");
            App.addOutput(error);
            return;
        }

        String numeMiles = AppState.getMilestoneNameByTicket(ticketID);
        if (numeMiles != null) {
            Milestone milestone = AppState.getMilestoneByName(numeMiles);
            if (milestone != null) {
                if (!milestone.getAssignedDevs().contains(username)) {
                    error.put("error", String.format(
                                    "Developer %s is not assigned to milestone %s.",
                                    username, numeMiles));
                    App.addOutput(error);
                    return;
                }

                if (milestone.isBlocked()) {
                    error.put("error", String.format(
                                    "Cannot assign ticket %d from blocked milestone %s.",
                                    ticketID, numeMiles));
                    App.addOutput(error);
                    return;
                }

                milestone.attachDeveloperByUsername(username);
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

    /**
     * Validates if the developer has access
     */
    private boolean validDev(final ExpertiseArea devArea, final ExpertiseArea ticketArea) {
        return switch (devArea) {
            case FULLSTACK ->
                    true;

            case FRONTEND ->
                    ticketArea == ExpertiseArea.FRONTEND || ticketArea == ExpertiseArea.DESIGN;
            case BACKEND ->

                    ticketArea == ExpertiseArea.BACKEND || ticketArea == ExpertiseArea.DB;
            case DESIGN ->

                    ticketArea == ExpertiseArea.DESIGN || ticketArea == ExpertiseArea.FRONTEND;
            case DB ->
                    ticketArea == ExpertiseArea.DB;

            case DEVOPS ->
                    ticketArea == ExpertiseArea.DEVOPS;
            
            default -> false;
        };
    }

    /**
     * Builds the list of required expertise areas for a ticket
     */
    private List<String> buildReqExp(final ExpertiseArea ticketArea) {
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

    /**
     * Validates if the developer has access
     */
    private boolean valDev(final Seniority seniority,
                                                          final BusinessPriority priority,
                                                          final Type type) {
        if (seniority == Seniority.SENIOR) {
            return true;
        }

        if (seniority == Seniority.JUNIOR) {
            if (type == Type.FEATURE_REQUEST) {
                return false;
            }
            return priority == BusinessPriority.LOW
                    || priority == BusinessPriority.MEDIUM;
        }

        if (seniority == Seniority.MID) {
            if (priority == BusinessPriority.CRITICAL) {
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * Builds the list of required seniority levels for a ticket
     */
    private List<String> reqSenLevels(final BusinessPriority priority,
                                                       final Type type) {
        List<String> level = new ArrayList<>();

        if (priority == BusinessPriority.CRITICAL || type == Type.FEATURE_REQUEST) {
            if (priority == BusinessPriority.CRITICAL) {
                level.add("SENIOR");
            } else if (type == Type.FEATURE_REQUEST) {
                level.add("MID");
                level.add("SENIOR");
            }
        } else if (priority == BusinessPriority.HIGH) {
            level.add("MID");
            level.add("SENIOR");
        } else {
            level.add("JUNIOR");
            level.add("MID");
            level.add("SENIOR");
        }

        level.sort(String::compareTo);
        return level;
    }

    /**
     * Undoes the assign ticket command
     */
    @Override
    public void undo() {
    }
}
