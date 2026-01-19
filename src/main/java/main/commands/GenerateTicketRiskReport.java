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
    private static final double MAX_BUG_SCORE = 12.0;
    private static final double MAX_FEATURE_REQUEST_SCORE = 20.0;
    private static final double MAX_UI_FEEDBACK_SCORE = 100.0;
    private static final double NORMALIZATION_FACTOR = 100.0;
    private static final double RISK_NEGLIGIBLE = 25.0;
    private static final double RISK_MODERATE = 50.0;
    private static final double RISK_SIGNIFICANT = 75.0;
    private static final int FREQ_RARE = 1;
    private static final int FREQ_OCCASIONAL = 2;
    private static final int FREQ_FREQUENT = 3;
    private static final int FREQ_ALWAYS = 4;
    private static final int SEVERITY_MINOR = 1;
    private static final int SEVERITY_MODERATE = 2;
    private static final int SEVERITY_SEVERE = 3;
    private static final int BUSINESS_S = 1;
    private static final int BUSINESS_M = 3;
    private static final int BUSINESS_L = 6;
    private static final int BUSINESS_XL = 10;
    private static final int CUSTOMER_LOW = 1;
    private static final int CUSTOMER_MEDIUM = 3;
    private static final int CUSTOMER_HIGH = 6;
    private static final int CUSTOMER_VERY_HIGH = 10;
    private static final int ELEVEN = 11;
    private final ObjectNode node;

    /**
     * Constructs a GenerateTicketRiskReport command with the given node
     */
    public GenerateTicketRiskReport(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the generate ticket risk report command
     */
    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        ObjectNode res = mapper.createObjectNode();
        res.put("command", "generateTicketRiskReport");
        res.put("username", username);
        res.put("timestamp", timestamp);

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

        Map<String, List<Double>> riskScores = new HashMap<>();
        riskScores.put("BUG", new ArrayList<>());
        riskScores.put("FEATURE_REQUEST", new ArrayList<>());
        riskScores.put("UI_FEEDBACK", new ArrayList<>());

        for (Ticket ticket : okTick) {
            String type = ticket.getType().toString();
            double baseScore = 0.0;
            double maxValue = 0.0;

            if (type.equals("BUG")) {
                Bug bug = (Bug) ticket;
                int frequency = getFrequencyValue(bug.getFrequency().toString());
                int severityFactor = getSeverityValue(bug.getSeverity().toString());

                baseScore = frequency * severityFactor;
                maxValue = MAX_BUG_SCORE;
            } else if (type.equals("FEATURE_REQUEST")) {
                FeatureRequest fr = (FeatureRequest) ticket;
                int businessValue = getBusinessValue(fr.getBusinessValue().toString());
                int customerDemand = getCustomerDemand(fr.getCustomerDemand().toString());

                baseScore = businessValue + customerDemand;
                maxValue = MAX_FEATURE_REQUEST_SCORE;
            } else if (type.equals("UI_FEEDBACK")) {
                UiFeedback ui = (UiFeedback) ticket;
                int businessValue = getBusinessValue(ui.getBusinessValue().toString());
                int usabilityScore = ui.getUsabilityScore();

                baseScore = (ELEVEN - usabilityScore) * businessValue;
                maxValue = MAX_UI_FEEDBACK_SCORE;
            }

            double normalizedScore = Math.min(NORMALIZATION_FACTOR,
                    (baseScore * NORMALIZATION_FACTOR) / maxValue);
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

        res.set("report", reportNode);
        App.addOutput(res);
    }

    /**
     * Undoes the generate ticket risk report command
     */
    @Override
    public void undo() {
    }

    /**
     * Returns the risk qualifier string for a given score
     */
    private String getRiskQualifier(final double score) {
        if (score < RISK_NEGLIGIBLE) {
            return "NEGLIGIBLE";
        } else if (score < RISK_MODERATE) {
            return "MODERATE";
        } else if (score < RISK_SIGNIFICANT) {
            return "SIGNIFICANT";
        } else {
            return "MAJOR";
        }
    }

    /**
     * Returns the integer value for a frequency string
     */
    private int getFrequencyValue(final String frequency) {
        switch (frequency) {
            case "RARE":
                return FREQ_RARE;
            case "OCCASIONAL":
                return FREQ_OCCASIONAL;
            case "FREQUENT":
                return FREQ_FREQUENT;
            case "ALWAYS":
                return FREQ_ALWAYS;
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
                return SEVERITY_MINOR;
            case "MODERATE":
                return SEVERITY_MODERATE;
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
                return BUSINESS_S;
            case "M":
                return BUSINESS_M;
            case "L":
                return BUSINESS_L;
            case "XL":
                return BUSINESS_XL;
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
                return CUSTOMER_LOW;
            case "MEDIUM":
                return CUSTOMER_MEDIUM;
            case "HIGH":
                return CUSTOMER_HIGH;
            case "VERY_HIGH":
                return CUSTOMER_VERY_HIGH;
            default:
                return 0;
        }
    }
}
