package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import java.util.ArrayList;
import java.util.List;
import main.enums.BusinessPriority;
import main.milestone.Milestone;
import main.ticket.Ticket;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public final class GeneratePerformanceReport implements Command {
    private static final double JUNIOR_SCORE_MULTIPLIER = 0.5;
    private static final double JUNIOR_SCORE_BONUS = 5.0;
    private static final double MID_SCORE_MULTIPLIER = 0.5;
    private static final double MID_HIGH_PRIORITY_MULTIPLIER = 0.7;
    private static final double MID_RESOLUTION_TIME_MULTIPLIER = 0.3;
    private static final double MID_SCORE_BONUS = 15.0;
    private static final double SENIOR_SCORE_MULTIPLIER = 0.5;
    private static final double SENIOR_HIGH_PRIORITY_MULTIPLIER = 1.0;
    private static final double SENIOR_RESOLUTION_TIME_MULTIPLIER = 0.5;
    private static final double SENIOR_SCORE_BONUS = 30.0;
    private static final double TICKET_TYPE_DIVISOR = 3.0;
    private static final double ROUNDING_FACTOR = 100.0;

    private final ObjectNode node;

    /**
     * Constructs a GeneratePerformanceReport command
     */
    public GeneratePerformanceReport(final ObjectNode node) {
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
        ObjectNode result = mapper.createObjectNode();
        result.put("command", "generatePerformanceReport");
        result.put("username", username);
        result.put("timestamp", timestamp);

        LocalDate repDate = LocalDate.parse(timestamp);
        LocalDate prevMonth = repDate.minusMonths(1).withDayOfMonth(1);
        LocalDate prevMonthEnd = repDate.minusMonths(1).withDayOfMonth(
                repDate.minusMonths(1).lengthOfMonth());
        List<String> subordinates = AppState.getManSubord(username);
        subordinates.sort(String::compareTo);
        ArrayNode arrReport = mapper.createArrayNode();

        for (String usrDevs : subordinates) {

            ObjectNode devNode = App.getDeveloperByUsername(usrDevs);

            if (devNode == null) {
                continue;
            }

            List<Ticket> closedTickets = new ArrayList<>();

            for (Ticket ticket : AppState.getTickets()) {

                if (ticket.getStatus().equals("CLOSED") && ticket.getAssignedTo() != null
                        && ticket.getAssignedTo().equals(usrDevs)
                        && ticket.getSolvedAt() != null) {

                    LocalDate solvedDate = ticket.getSolvedAt();
                    if (!solvedDate.isBefore(prevMonth)

                            && !solvedDate.isAfter(prevMonthEnd)) {
                        closedTickets.add(ticket);
                    }
                }
            }

            int totalClosed = closedTickets.size();
            double averageResolutionTime = 0.0;
            double performanceScore = 0.0;

            if (totalClosed > 0) {

                int nrDays = 0;
                for (Ticket ticket : closedTickets) {
                    int days = (int) ChronoUnit.DAYS.between(ticket.getAssignedAt(),
                            ticket.getSolvedAt()) + 1;
                    nrDays += days;
                }
                averageResolutionTime = nrDays / (double) totalClosed;

                String seniority = devNode.get("seniority").asText();


                if (seniority.equals("JUNIOR")) {

                    int bugCount = 0;
                    int featureCount = 0;
                    int uiCount = 0;

                    for (Ticket ticket : closedTickets) {

                        String type = ticket.getType().toString();

                        if (type.equals("BUG")) {
                            bugCount++;
                        } else if (type.equals("FEATURE_REQUEST")) {
                            featureCount++;
                        } else if (type.equals("UI_FEEDBACK")) {
                            uiCount++;
                        }
                    }

                    double ticketDiversity = tickFactor(bugCount, featureCount, uiCount);
                    performanceScore = Math.max(0, JUNIOR_SCORE_MULTIPLIER * totalClosed
                                    - ticketDiversity) + JUNIOR_SCORE_BONUS;
                } else if (seniority.equals("MID")) {
                    int highCnt = 0;
                    for (Ticket ticket : closedTickets) {
                        BusinessPriority priority =
                                escPrior(ticket, ticket.getSolvedAt());
                        if (priority == BusinessPriority.HIGH
                                || priority == BusinessPriority.CRITICAL) {
                            highCnt++;
                        }
                    }
                    performanceScore = Math.max(0,
                            MID_SCORE_MULTIPLIER * totalClosed
                                    + MID_HIGH_PRIORITY_MULTIPLIER * highCnt
                                    - MID_RESOLUTION_TIME_MULTIPLIER * averageResolutionTime)
                            + MID_SCORE_BONUS;
                } else if (seniority.equals("SENIOR")) {
                    int highCnt = 0;
                    for (Ticket ticket : closedTickets) {
                        BusinessPriority priority =
                                escPrior(ticket, ticket.getSolvedAt());
                        if (priority == BusinessPriority.HIGH
                                || priority == BusinessPriority.CRITICAL) {
                            highCnt++;
                        }
                    }
                    performanceScore = Math.max(0, SENIOR_SCORE_MULTIPLIER * totalClosed
                                    + SENIOR_HIGH_PRIORITY_MULTIPLIER * highCnt
                                    - SENIOR_RESOLUTION_TIME_MULTIPLIER * averageResolutionTime)
                            + SENIOR_SCORE_BONUS;
                }
            }

            devNode.put("performanceScore",
                    Math.round(performanceScore * ROUNDING_FACTOR) / ROUNDING_FACTOR);

            ObjectNode devReport = mapper.createObjectNode();
            
            devReport.put("username", usrDevs);
            devReport.put("closedTickets", totalClosed);
            devReport.put("averageResolutionTime",
                    Math.round(averageResolutionTime * ROUNDING_FACTOR) / ROUNDING_FACTOR);
            devReport.put("performanceScore",
                    Math.round(performanceScore * ROUNDING_FACTOR) / ROUNDING_FACTOR);
            devReport.put("seniority", devNode.get("seniority").asText());
            arrReport.add(devReport);
        }

        result.set("report", arrReport);
        App.addOutput(result);
    }

    /**
     * Undo operation is not implemented for this command
     */
    @Override
    public void undo() {

    }

    /**
     * Calculates the average number of resolved tickets by type
     */
    private static double avgTicketType(final int bug, final int feature,
                                                    final int ui) {

        return (bug + feature + ui) / TICKET_TYPE_DIVISOR;
    }

    /**
     * Computes the standard deviation for the resolved ticket types
     */
    private static double standardDeviation(final int bug, final int feature,
                                            final int ui) {
        
        double mean = avgTicketType(bug, feature, ui);
        double varr = (Math.pow(bug - mean, 2)
                + Math.pow(feature - mean, 2) + Math.pow(ui - mean, 2)) / TICKET_TYPE_DIVISOR;
        return Math.sqrt(varr);
    }

    /**
     * Calculates the diversity factor for ticket types resolved by a developer
     */
    private static double tickFactor(final int bug, final int feature,
                                                final int ui) {
        
        double mean = avgTicketType(bug, feature, ui);
        
        if (mean == 0.0) {
            return 0.0;
        }
        
        double std = standardDeviation(bug, feature, ui);
        return std / mean;
    }

    /**
     * Determines the escalated business priority for a ticket based on milestone and dates
     */
    private BusinessPriority escPrior(final Ticket ticket,
                                                  final LocalDate solvedDate) {
        String nameMiles = AppState.getMilestoneNameByTicket(ticket.getId());
        if (nameMiles == null) {
            return ticket.getBusinessPriority();
        }

        Milestone milestone = AppState.getMilestoneByName(nameMiles);
        if (milestone == null || milestone.isBlocked()) {
            return ticket.getBusinessPriority();
        }

        BusinessPriority pr = ticket.getBusinessPriority();
        long remDaysMiles = ChronoUnit.DAYS.between(milestone.getCreatedAt(),
                solvedDate) + 1;
        long remDays = ChronoUnit.DAYS.between(solvedDate, milestone.getDueDate());
        
        if (remDays == 1) {
            return BusinessPriority.CRITICAL;
        }

        if (remDaysMiles >= 2) {
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
