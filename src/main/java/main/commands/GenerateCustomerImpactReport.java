package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.ticket.FeatureRequest;
import main.ticket.Ticket;
import main.ticket.UiFeedback;
import main.App;
import main.AppState;
import main.ticket.Bug;;

public final class GenerateCustomerImpactReport implements Command {
    private static final double BUG_MAX_VALUE = 48.0;
    private static final double MAXI = 100;
    private static final int PRIORITY_HIGH = 3;
    private static final int PRIORITY_CRITICAL = 4;
    private static final int SEVERITY_SEVERE = 3;
    private static final int BUSINESS_VALUE_M = 3;
    private static final int BUSINESS_VALUE_L = 6;
    private static final int BUSINESS_VALUE_XL = 10;
    private static final int CUSTOMER_DEMAND_MEDIUM = 3;
    private static final int CUSTOMER_DEMAND_HIGH = 6;
    private static final int CUSTOMER_DEMAND_VERY_HIGH = 10;

    private final ObjectNode node;

    /**
     * Constructs a GenerateCustomerImpactReport command with the given node
     */
    public GenerateCustomerImpactReport(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the generate customer impact report command for the specified user
     */
    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();
        ObjectNode result = mapper.createObjectNode();
        result.put("command", "generateCustomerImpactReport");
        result.put("username", username);
        result.put("timestamp", timestamp);

        List<Ticket> okTick = new ArrayList<>();
        for (Ticket ticket : AppState.getTickets()) {
            String status = ticket.getStatus();
            if (status.equals("OPEN") || status.equals("IN_PROGRESS")) {
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
            String priority = ticket.getBusinessPriority().toString();
            ticketsByPriority.put(priority, ticketsByPriority.get(priority) + 1);
        }

        Map<String, List<Double>> imScores = new HashMap<>();
        imScores.put("BUG", new ArrayList<>());
        imScores.put("FEATURE_REQUEST", new ArrayList<>());
        imScores.put("UI_FEEDBACK", new ArrayList<>());

        for (Ticket ticket : okTick) {
            String type = ticket.getType().toString();
            double scBaza = 0.0;
            double maxi = 0.0;

            if (type.equals("BUG")) {
                Bug bug = (Bug) ticket;
                int frequency = getFrequencyValue(bug.getFrequency().toString());
                int businessPriority = getPriorityValue(bug.getBusinessPriority().toString());
                int severityFactor = getSeverityValue(bug.getSeverity().toString());

                scBaza = frequency * businessPriority * severityFactor;
                maxi = BUG_MAX_VALUE;
            } else if (type.equals("FEATURE_REQUEST")) {
                FeatureRequest fr = (FeatureRequest) ticket;
                int businessValue = getBusinessValue(fr.getBusinessValue().toString());
                int customerDemand = getCustomerDemand(fr.getCustomerDemand().toString());
                scBaza = businessValue * customerDemand;
                maxi = MAXI;

            } else if (type.equals("UI_FEEDBACK")) {
                UiFeedback ui = (UiFeedback) ticket;
                int businessValue = getBusinessValue(ui.getBusinessValue().toString());
                int usabilityScore = ui.getUsabilityScore();
                scBaza = businessValue * usabilityScore;
                maxi = MAXI;
            }

            double normalizedScore = Math.min(MAXI,
                    (scBaza * MAXI) / maxi);
            imScores.get(type).add(normalizedScore);
        }

        Map<String, Double> customerImpactByType = new HashMap<>();
        for (String type : imScores.keySet()) {
            List<Double> scores = imScores.get(type);
            double average = 0.0;
            if (!scores.isEmpty()) {
                average = scores.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
            }
            customerImpactByType.put(type, Math.round(average * MAXI) / MAXI);
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

        ObjectNode impactNode = mapper.createObjectNode();
        impactNode.put("BUG", customerImpactByType.get("BUG"));
        impactNode.put("FEATURE_REQUEST", customerImpactByType.get("FEATURE_REQUEST"));
        impactNode.put("UI_FEEDBACK", customerImpactByType.get("UI_FEEDBACK"));
        reportNode.set("customerImpactByType", impactNode);

        result.set("report", reportNode);
        App.addOutput(result);
    }

    /**
     * Undoes the generate customer impact report command
     */
    @Override
    public void undo() {
    }

    /**
     * Returns the value for a frequency string
     */
    private int getFrequencyValue(final String frequency) {
        switch (frequency) {
            case "RARE":
                return 1;
            case "OCCASIONAL":
                return 2;
            case "FREQUENT":
                return PRIORITY_HIGH;
            case "ALWAYS":
                return PRIORITY_CRITICAL;
            default:
                return 0;
        }
    }

    /**
     * Returns the value for a priority string
     */
    private int getPriorityValue(final String priority) {
        switch (priority) {
            case "LOW":
                return 1;
            case "MEDIUM":
                return 2;
            case "HIGH":
                return PRIORITY_HIGH;
            case "CRITICAL":
                return PRIORITY_CRITICAL;
            default:
                return 0;
        }
    }

    /**
     * Returns the value for a severity string
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
     * Returns the value for a business value string
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
     * Returns the value for a customer demand string
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
}
