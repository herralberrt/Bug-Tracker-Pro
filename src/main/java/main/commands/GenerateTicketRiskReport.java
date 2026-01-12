package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.ticket.Bug;
import main.ticket.FeatureRequest;
import main.ticket.Ticket;
import main.ticket.UiFeedback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GenerateTicketRiskReport implements Command {
    private final ObjectNode node;

    public GenerateTicketRiskReport(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        ObjectNode result = mapper.createObjectNode();
        result.put("command", "generateTicketRiskReport");
        result.put("username", username);
        result.put("timestamp", timestamp);

        List<Ticket> eligibleTickets = new ArrayList<>();
        for (Ticket ticket : AppState.getTickets()) {
            String status = ticket.getStatus();
            if (status.equals("OPEN") || status.equals("IN_PROGRESS")) {
                eligibleTickets.add(ticket);
            }
        }

        int totalTickets = eligibleTickets.size();
        Map<String, Integer> ticketsByType = new HashMap<>();
        ticketsByType.put("BUG", 0);
        ticketsByType.put("FEATURE_REQUEST", 0);
        ticketsByType.put("UI_FEEDBACK", 0);

        Map<String, Integer> ticketsByPriority = new HashMap<>();
        ticketsByPriority.put("LOW", 0);
        ticketsByPriority.put("MEDIUM", 0);
        ticketsByPriority.put("HIGH", 0);
        ticketsByPriority.put("CRITICAL", 0);

        for (Ticket ticket : eligibleTickets) {
            String type = ticket.getType().toString();
            ticketsByType.put(type, ticketsByType.get(type) + 1);

            String priority = ticket.getBusinessPriority().toString();
            ticketsByPriority.put(priority, ticketsByPriority.get(priority) + 1);
        }

        Map<String, List<Double>> riskScores = new HashMap<>();
        riskScores.put("BUG", new ArrayList<>());
        riskScores.put("FEATURE_REQUEST", new ArrayList<>());
        riskScores.put("UI_FEEDBACK", new ArrayList<>());

        for (Ticket ticket : eligibleTickets) {
            String type = ticket.getType().toString();
            double baseScore = 0.0;
            double maxValue = 0.0;

            if (type.equals("BUG")) {
                Bug bug = (Bug) ticket;
                int frequency = getFrequencyValue(bug.getFrequency().toString());
                int severityFactor = getSeverityValue(bug.getSeverity().toString());

                baseScore = frequency * severityFactor;
                maxValue = 12.0;
            } else if (type.equals("FEATURE_REQUEST")) {
                FeatureRequest fr = (FeatureRequest) ticket;
                int businessValue = getBusinessValue(fr.getBusinessValue().toString());
                int customerDemand = getCustomerDemand(fr.getCustomerDemand().toString());

                baseScore = businessValue + customerDemand;
                maxValue = 20.0;
            } else if (type.equals("UI_FEEDBACK")) {
                UiFeedback ui = (UiFeedback) ticket;
                int businessValue = getBusinessValue(ui.getBusinessValue().toString());
                int usabilityScore = ui.getUsabilityScore();

                baseScore = (11 - usabilityScore) * businessValue;
                maxValue = 100.0;
            }

            double normalizedScore = Math.min(100.0, (baseScore * 100.0) / maxValue);
            riskScores.get(type).add(normalizedScore);
        }

        Map<String, String> riskByType = new HashMap<>();
        for (String type : riskScores.keySet()) {
            List<Double> scores = riskScores.get(type);
            double average = 0.0;
            if (!scores.isEmpty()) {
                average = scores.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
            }
            riskByType.put(type, getRiskQualifier(average));
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

        ObjectNode riskNode = mapper.createObjectNode();
        riskNode.put("BUG", riskByType.get("BUG"));
        riskNode.put("FEATURE_REQUEST", riskByType.get("FEATURE_REQUEST"));
        riskNode.put("UI_FEEDBACK", riskByType.get("UI_FEEDBACK"));
        reportNode.set("riskByType", riskNode);

        result.set("report", reportNode);
        App.addOutput(result);
    }

    @Override
    public void undo() {
    }

    private String getRiskQualifier(double score) {
        if (score < 25.0) {
            return "NEGLIGIBLE";
        } else if (score < 50.0) {
            return "MODERATE";
        } else if (score < 75.0) {
            return "SIGNIFICANT";
        } else {
            return "MAJOR";
        }
    }

    private int getFrequencyValue(String frequency) {
        switch (frequency) {
            case "RARE": return 1;
            case "OCCASIONAL": return 2;
            case "FREQUENT": return 3;
            case "ALWAYS": return 4;
            default: return 0;
        }
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "LOW": return 1;
            case "MEDIUM": return 2;
            case "HIGH": return 3;
            case "CRITICAL": return 4;
            default: return 0;
        }
    }

    private int getSeverityValue(String severity) {
        switch (severity) {
            case "MINOR": return 1;
            case "MODERATE": return 2;
            case "SEVERE": return 3;
            default: return 0;
        }
    }

    private int getBusinessValue(String businessValue) {
        switch (businessValue) {
            case "S": return 1;
            case "M": return 3;
            case "L": return 6;
            case "XL": return 10;
            default: return 0;
        }
    }

    private int getCustomerDemand(String customerDemand) {
        switch (customerDemand) {
            case "LOW": return 1;
            case "MEDIUM": return 3;
            case "HIGH": return 6;
            case "VERY_HIGH": return 10;
            default: return 0;
        }
    }
}
