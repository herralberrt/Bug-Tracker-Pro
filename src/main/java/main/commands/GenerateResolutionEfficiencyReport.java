package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.enums.BusinessPriority;
import main.milestone.Milestone;
import main.ticket.Bug;
import main.ticket.FeatureRequest;
import main.ticket.Ticket;
import main.ticket.UiFeedback;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public final class GenerateResolutionEfficiencyReport implements Command {
    private static final double BUG_SCORE_MULTIPLIER = 10.0;
    private static final double BUG_MAX_VALUE = 70.0;
    private static final double FEATURE_REQUEST_MAX_VALUE = 20.0;
    private static final double UI_FEEDBACK_MAX_VALUE = 20.0;
    private static final double MAXI = 100.0;
    private static final double ROUNDING_FACTOR = 100.0;
    private static final int FREQUENCY_ALWAYS = 4;
    private static final int FREQUENCY_FREQUENT = 3;
    private static final int SEVERITY_SEVERE = 3;
    private static final int BUSINESS_VALUE_M = 3;
    private static final int BUSINESS_VALUE_L = 6;
    private static final int BUSINESS_VALUE_XL = 10;
    private static final int CUSTOMER_DEMAND_MEDIUM = 3;
    private static final int CUSTOMER_DEMAND_HIGH = 6;
    private static final int CUSTOMER_DEMAND_VERY_HIGH = 10;
    private final ObjectNode node;

    /**
     * Constructs a GenerateResolutionEfficiencyReport command
     */
    public GenerateResolutionEfficiencyReport(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the command
     */
    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();
        ObjectNode res = mapper.createObjectNode();
        
        res.put("command", "generateResolutionEfficiencyReport");
        res.put("username", username);
        res.put("timestamp", timestamp);

        List<Ticket> okTick = new ArrayList<>();

        for (Ticket ticket : AppState.getTickets()) {

            String status = ticket.getStatus();

            if (status.equals("RESOLVED") || status.equals("CLOSED")) {
                okTick.add(ticket);
            }
        }
        int totalTickets = okTick.size();
        Map<String, Integer> ticketsByType = new HashMap<>();

        ticketsByType.put("BUG", 0);
        ticketsByType.put("FEATURE_REQUEST", 0);
        ticketsByType.put("UI_FEEDBACK", 0);

        Map<String, Integer> ticketsByPriority = new HashMap<>();

        ticketsByPriority.put("LOW", 0);
        ticketsByPriority.put("MEDIUM", 0);
        ticketsByPriority.put("HIGH", 0);
        ticketsByPriority.put("CRITICAL", 0);

        for (Ticket ticket : okTick) {
            String type = ticket.getType().toString();
            ticketsByType.put(type, ticketsByType.get(type) + 1);

            BusinessPriority priority = getEscPriority(ticket, ticket.getSolvedAt());
            ticketsByPriority.put(priority.toString(),
                    ticketsByPriority.get(priority.toString()) + 1);
        }

        Map<String, List<Double>> efficiencyScores = new HashMap<>();

        efficiencyScores.put("BUG", new ArrayList<>());
        efficiencyScores.put("FEATURE_REQUEST", new ArrayList<>());
        efficiencyScores.put("UI_FEEDBACK", new ArrayList<>());

        for (Ticket ticket : okTick) {

            String type = ticket.getType().toString();
            LocalDate assignedDate = ticket.getAssignedAt();
            LocalDate solvedDate = ticket.getSolvedAt();
            int daysToResolve = (int) ChronoUnit.DAYS.between(assignedDate, solvedDate) + 1;
            double baseScore = 0.0;
            double maxValue = 0.0;

            if (type.equals("BUG")) {
                Bug bug = (Bug) ticket;
                int frequency = getFrequencyValue(bug.getFrequency().toString());
                int severityFactor = getSeverityValue(bug.getSeverity().toString());
                baseScore = (frequency + severityFactor) * BUG_SCORE_MULTIPLIER / daysToResolve;
                maxValue = BUG_MAX_VALUE;

            } else if (type.equals("FEATURE_REQUEST")) {
                FeatureRequest fr = (FeatureRequest) ticket;
                int businessValue = getBusinessValue(fr.getBusinessValue().toString());
                int customerDemand = getCustomerDemand(fr.getCustomerDemand().toString());
                baseScore = (businessValue + customerDemand) / (double) daysToResolve;
                maxValue = FEATURE_REQUEST_MAX_VALUE;

            } else if (type.equals("UI_FEEDBACK")) {

                UiFeedback ui = (UiFeedback) ticket;
                int businessValue = getBusinessValue(ui.getBusinessValue().toString());
                int usabilityScore = ui.getUsabilityScore();
                baseScore = (usabilityScore + businessValue) / (double) daysToResolve;
                maxValue = UI_FEEDBACK_MAX_VALUE;
            }

            double normalizedScore = Math.min(MAXI,
                    (baseScore * MAXI) / maxValue);
            efficiencyScores.get(type).add(normalizedScore);
        }

        Map<String, Double> efficiencyByType = new HashMap<>();

        for (String type : efficiencyScores.keySet()) {

            List<Double> scores = efficiencyScores.get(type);
            double average = 0.0;

            if (!scores.isEmpty()) {
                average = scores.stream().mapToDouble(Double::doubleValue)
                        .average().orElse(0.0);
            }
            efficiencyByType.put(type, Math.round(average * ROUNDING_FACTOR) / ROUNDING_FACTOR);
        }

        ObjectNode reportNode = mapper.createObjectNode();
        reportNode.put("totalTickets", totalTickets);

        ObjectNode typeNode = mapper.createObjectNode();
        typeNode.put("BUG", ticketsByType.get("BUG"));
        typeNode.put("FEATURE_REQUEST", ticketsByType.get("FEATURE_REQUEST"));
        typeNode.put("UI_FEEDBACK", ticketsByType.get("UI_FEEDBACK"));
        reportNode.set("ticketsByType", typeNode);

        ObjectNode priorityNode = mapper.createObjectNode();
        priorityNode.put("LOW", ticketsByPriority.get("LOW"));
        priorityNode.put("MEDIUM", ticketsByPriority.get("MEDIUM"));
        priorityNode.put("HIGH", ticketsByPriority.get("HIGH"));
        priorityNode.put("CRITICAL", ticketsByPriority.get("CRITICAL"));
        reportNode.set("ticketsByPriority", priorityNode);

        ObjectNode efficiencyNode = mapper.createObjectNode();
        efficiencyNode.put("BUG", efficiencyByType.get("BUG"));
        efficiencyNode.put("FEATURE_REQUEST", efficiencyByType.get("FEATURE_REQUEST"));
        efficiencyNode.put("UI_FEEDBACK", efficiencyByType.get("UI_FEEDBACK"));
        reportNode.set("efficiencyByType", efficiencyNode);

        res.set("report", reportNode);
        App.addOutput(res);
    }

    /**
     * Undo operation is not implemented for this command
     */
    @Override
    public void undo() {
    }

    /**
     * Returns the value for a given bug frequency
     */
    private int getFrequencyValue(final String frequency) {
        switch (frequency) {
            case "RARE":
                return 1;

            case "OCCASIONAL":
                return 2;

            case "FREQUENT":
                return FREQUENCY_FREQUENT;

            case "ALWAYS":
                return FREQUENCY_ALWAYS;

            default:
                return 0;
        }
    }

    /**
     * Returns the value for a given bug severity
     */
    private int getSeverityValue(final String severity) {
        switch (severity) {
            case "MINOR":
                return 1;

            case "MODERATE":
                return 2;

            case "SEVERE":
                return SEVERITY_SEVERE;

            default:
                return 0;
        }
    }

    /**
     * Returns the value for a given business value
     */
    private int getBusinessValue(final String businessValue) {
        switch (businessValue) {
            case "S":
                return 1;

            case "M":
                return BUSINESS_VALUE_M;

            case "L":
                return BUSINESS_VALUE_L;

            case "XL":
                return BUSINESS_VALUE_XL;

            default:
                return 0;
        }
    }

    /**
     * Returns the value for a given customer demand
     */
    private int getCustomerDemand(final String customerDemand) {
        switch (customerDemand) {

            case "LOW":
                return 1;

            case "MEDIUM":
                return CUSTOMER_DEMAND_MEDIUM;

            case "HIGH":
                return CUSTOMER_DEMAND_HIGH;

            case "VERY_HIGH":
                return CUSTOMER_DEMAND_VERY_HIGH;

            default:
                return 0;
        }
    }

    /**
     * Determines the escalated business priority for a ticket based on milestone and dates
     */
    private BusinessPriority getEscPriority(final Ticket ticket,
                                                  final LocalDate solvedDate) {
        
        String numeMiles = AppState.getMilestoneNameByTicket(ticket.getId());
        
        if (numeMiles == null) {
            return ticket.getBusinessPriority();
        }

        Milestone milestone = AppState.getMilestoneByName(numeMiles);
        if (milestone == null || milestone.isBlocked()) {
            return ticket.getBusinessPriority();
        }

        BusinessPriority pr = ticket.getBusinessPriority();
        long remMilesDays = ChronoUnit.DAYS.between(
                milestone.getCreatedAt(), solvedDate) + 1;

        long remDays = ChronoUnit.DAYS.between(solvedDate, milestone.getDueDate());
        if (remDays == 1) {
            return BusinessPriority.CRITICAL;
        }

        if (remMilesDays >= 2) {
            return switch (pr) {
                case LOW ->
                        BusinessPriority.MEDIUM;

                case MEDIUM ->
                        BusinessPriority.HIGH;

                case HIGH ->
                        BusinessPriority.CRITICAL;

                case CRITICAL ->
                        BusinessPriority.CRITICAL;
            };
        }

        return pr;
    }
}
