package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.ticket.Bug;
import main.ticket.FeatureRequest;
import main.ticket.Ticket;
import main.ticket.UiFeedback;

public final class AppStabilityReport implements Command {
    private static final double BUG_RISK_MAX = 12.0;
    private static final double BUG_IMPACT_MAX = 48.0;
    private static final double FEATURE_RISK_MAX = 20.0;
    private static final double FEATURE_IMPACT_MAX = 100.0;
    private static final double UI_RISK_MAX = 100.0;
    private static final double UI_IMPACT_MAX = 100.0;
    private static final int USABILITY_SCORE_BASE = 11;
    private static final double NORMALIZE_MAX = 100.0;
    private static final double IMPACT_THRESHOLD = 50.0;
    private static final double RISK_NEGLIGIBLE = 25.0;
    private static final double RISK_MODERATE = 50.0;
    private static final double RISK_SIGNIFICANT = 75.0;
    private static final int FREQ_RARE = 1;
    private static final int FREQ_OCCASIONAL = 2;
    private static final int FREQ_FREQUENT = 3;
    private static final int FREQ_ALWAYS = 4;
    private static final int PRIORITY_LOW = 1;
    private static final int PRIORITY_MEDIUM = 2;
    private static final int PRIORITY_HIGH = 3;
    private static final int PRIORITY_CRITICAL = 4;
    private static final int SEVERITY_MINOR = 1;
    private static final int SEVERITY_MODERATE = 2;
    private static final int SEVERITY_SEVERE = 3;
    private static final int BUSINESS_S = 1;
    private static final int BUSINESS_M = 3;
    private static final int BUSINESS_L = 6;
    private static final int BUSINESS_XL = 10;
    private static final int DEMAND_LOW = 1;
    private static final int DEMAND_MEDIUM = 3;
    private static final int DEMAND_HIGH = 6;
    private static final int DEMAND_VERY_HIGH = 10;

    private final ObjectNode node;

    /**
     * Constructs an AppStabilityReport command
     */
    public AppStabilityReport(final ObjectNode node) {
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

        res.put("command", "appStabilityReport");
        res.put("username", username);
        res.put("timestamp", timestamp);

        List<Ticket> okTicket = new ArrayList<>();
        for (Ticket ticket : AppState.getTickets()) {
            String status = ticket.getStatus();
            if (status.equals("OPEN") || status.equals("IN_PROGRESS")) {
                okTicket.add(ticket);
            }
        }

        int totalOpenTickets = okTicket.size();

        if (totalOpenTickets == 0) {
            ObjectNode reportNode = mapper.createObjectNode();

            reportNode.put("totalOpenTickets", 0);

            ObjectNode typeNode = mapper.createObjectNode();
            typeNode.put("BUG", 0);
            typeNode.put("FEATURE_REQUEST", 0);
            typeNode.put("UI_FEEDBACK", 0);
            reportNode.set("openTicketsByType", typeNode);

            ObjectNode priorityNode = mapper.createObjectNode();
            priorityNode.put("LOW", 0);
            priorityNode.put("MEDIUM", 0);
            priorityNode.put("HIGH", 0);
            priorityNode.put("CRITICAL", 0);
            reportNode.set("openTicketsByPriority", priorityNode);

            ObjectNode riskNode = mapper.createObjectNode();
            riskNode.put("BUG", "NEGLIGIBLE");
            riskNode.put("FEATURE_REQUEST", "NEGLIGIBLE");
            riskNode.put("UI_FEEDBACK", "NEGLIGIBLE");
            reportNode.set("riskByType", riskNode);

            ObjectNode impactNode = mapper.createObjectNode();
            impactNode.put("BUG", 0.0);
            impactNode.put("FEATURE_REQUEST", 0.0);
            impactNode.put("UI_FEEDBACK", 0.0);
            reportNode.set("impactByType", impactNode);

            reportNode.put("appStability", "STABLE");
            res.set("report", reportNode);
            App.addOutput(res);
            AppState.loseInvestors();
            return;
        }

        Map<String, Integer> ticketsByType = new HashMap<>();
        ticketsByType.put("BUG", 0);
        ticketsByType.put("FEATURE_REQUEST", 0);
        ticketsByType.put("UI_FEEDBACK", 0);

        Map<String, Integer> ticketsByPriority = new HashMap<>();
        ticketsByPriority.put("LOW", 0);
        ticketsByPriority.put("MEDIUM", 0);
        ticketsByPriority.put("HIGH", 0);
        ticketsByPriority.put("CRITICAL", 0);

        for (Ticket ticket : okTicket) {
            String type = ticket.getType().toString();
            ticketsByType.put(type, ticketsByType.get(type) + 1);

            String priority = ticket.getBusinessPriority().toString();
            ticketsByPriority.put(priority, ticketsByPriority.get(priority) + 1);
        }

        Map<String, List<Double>> riskScores = new HashMap<>();
        riskScores.put("BUG", new ArrayList<>());
        riskScores.put("FEATURE_REQUEST", new ArrayList<>());
        riskScores.put("UI_FEEDBACK", new ArrayList<>());

        Map<String, List<Double>> impactScores = new HashMap<>();
        impactScores.put("BUG", new ArrayList<>());
        impactScores.put("FEATURE_REQUEST", new ArrayList<>());
        impactScores.put("UI_FEEDBACK", new ArrayList<>());

        for (Ticket ticket : okTicket) {
            String type = ticket.getType().toString();
            double riskBaseScore = 0.0;
            double riskMaxValue = 0.0;
            double impactBaseScore = 0.0;
            double impactMaxValue = 0.0;

            if (type.equals("BUG")) {
                Bug bug = (Bug) ticket;
                int frequency = getFrequencyValue(bug.getFrequency().toString());
                int businessPriority = getPriorityValue(bug.getBusinessPriority().toString());
                int severityFactor = getSeverityValue(bug.getSeverity().toString());

                riskBaseScore = frequency * severityFactor;
                riskMaxValue = BUG_RISK_MAX;

                impactBaseScore = frequency * businessPriority * severityFactor;
                impactMaxValue = BUG_IMPACT_MAX;
            } else if (type.equals("FEATURE_REQUEST")) {
                FeatureRequest fr = (FeatureRequest) ticket;
                int businessValue = getBusinessValue(fr.getBusinessValue().toString());
                int customerDemand = getCustomerDemand(fr.getCustomerDemand().toString());

                riskBaseScore = businessValue + customerDemand;
                riskMaxValue = FEATURE_RISK_MAX;

                impactBaseScore = businessValue * customerDemand;
                impactMaxValue = FEATURE_IMPACT_MAX;
            } else if (type.equals("UI_FEEDBACK")) {
                UiFeedback ui = (UiFeedback) ticket;
                int businessValue = getBusinessValue(ui.getBusinessValue().toString());
                int usabilityScore = ui.getUsabilityScore();

                riskBaseScore = (USABILITY_SCORE_BASE - usabilityScore) * businessValue;
                riskMaxValue = UI_RISK_MAX;

                impactBaseScore = businessValue * usabilityScore;
                impactMaxValue = UI_IMPACT_MAX;
            }

            double normalizedRisk = Math.min(NORMALIZE_MAX,
                    (riskBaseScore * NORMALIZE_MAX) / riskMaxValue);
            riskScores.get(type).add(normalizedRisk);

            double normalizedImpact = Math.min(NORMALIZE_MAX,
                    (impactBaseScore * NORMALIZE_MAX) / impactMaxValue);
            impactScores.get(type).add(normalizedImpact);
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

        Map<String, Double> impactByType = new HashMap<>();
        for (String type : impactScores.keySet()) {
            List<Double> scores = impactScores.get(type);
            double average = 0.0;
            if (!scores.isEmpty()) {
                average = scores.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
            }
            impactByType.put(type, Math.round(average * 100.0) / 100.0);
        }

        String appStability = detStab(riskByType, impactByType);

        ObjectNode reportNode = mapper.createObjectNode();
        reportNode.put("totalOpenTickets", totalOpenTickets);

        ObjectNode typeNode = mapper.createObjectNode();
        typeNode.put("BUG", ticketsByType.get("BUG"));
        typeNode.put("FEATURE_REQUEST", ticketsByType.get("FEATURE_REQUEST"));
        typeNode.put("UI_FEEDBACK", ticketsByType.get("UI_FEEDBACK"));
        reportNode.set("openTicketsByType", typeNode);

        ObjectNode priorityNode = mapper.createObjectNode();
        priorityNode.put("LOW", ticketsByPriority.get("LOW"));
        priorityNode.put("MEDIUM", ticketsByPriority.get("MEDIUM"));
        priorityNode.put("HIGH", ticketsByPriority.get("HIGH"));
        priorityNode.put("CRITICAL", ticketsByPriority.get("CRITICAL"));
        reportNode.set("openTicketsByPriority", priorityNode);

        ObjectNode riskNode = mapper.createObjectNode();
        riskNode.put("BUG", riskByType.get("BUG"));
        riskNode.put("FEATURE_REQUEST", riskByType.get("FEATURE_REQUEST"));
        riskNode.put("UI_FEEDBACK", riskByType.get("UI_FEEDBACK"));
        reportNode.set("riskByType", riskNode);

        ObjectNode impactNode = mapper.createObjectNode();
        impactNode.put("BUG", impactByType.get("BUG"));
        impactNode.put("FEATURE_REQUEST", impactByType.get("FEATURE_REQUEST"));
        impactNode.put("UI_FEEDBACK", impactByType.get("UI_FEEDBACK"));
        reportNode.set("impactByType", impactNode);

        reportNode.put("appStability", appStability);
        res.set("report", reportNode);
        App.addOutput(res);

        if (appStability.equals("STABLE")) {
            AppState.loseInvestors();
        }
    }

    /**
     * Undo operation is not implemented for this command
     */
    @Override
    public void undo() {
    }

    /**
     * Determines the overall application stability based on risk and impact values
     */
    private String detStab(final Map<String, String> riskByType,
                                      final Map<String, Double> impactByType) {

        boolean signi = false;
        boolean neg = true;
        boolean below = true;

        for (String risk : riskByType.values()) {
            if (risk.equals("SIGNIFICANT") || risk.equals("MAJOR")) {
                signi = true;
            }
            if (!risk.equals("NEGLIGIBLE")) {
                neg = false;
            }
        }

        for (Double impact : impactByType.values()) {
            if (impact >= IMPACT_THRESHOLD) {
                below = false;
                break;
            }
        }

        if (signi) {
            return "UNSTABLE";
        }

        if (neg && below) {
            return "STABLE";
        }
        return "PARTIALLY STABLE";
    }

    /**
     * Returns a risk qualifier based on the given risk score
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
     * Returns the value for a given bug frequency
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
     * Returns the value for a given ticket priority
     */
    private int getPriorityValue(final String priority) {
        switch (priority) {
            case "LOW":
                return PRIORITY_LOW;
            case "MEDIUM":
                return PRIORITY_MEDIUM;
            case "HIGH":
                return PRIORITY_HIGH;
            case "CRITICAL":
                return PRIORITY_CRITICAL;
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
     * Returns the value for a given business value
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
     * Returns the value for a given customer demand
     */
    private int getCustomerDemand(final String customerDemand) {
        switch (customerDemand) {
            case "LOW":
                return DEMAND_LOW;
            case "MEDIUM":
                return DEMAND_MEDIUM;
            case "HIGH":
                return DEMAND_HIGH;
            case "VERY_HIGH":
                return DEMAND_VERY_HIGH;
            default:
                return 0;
        }
    }
}
