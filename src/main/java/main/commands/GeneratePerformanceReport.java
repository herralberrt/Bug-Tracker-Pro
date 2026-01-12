package main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.BusinessPriority;
import main.milestone.Milestone;
import main.ticket.Ticket;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class GeneratePerformanceReport implements Command {

    private final ObjectNode node;

    public GeneratePerformanceReport(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        ObjectNode result = mapper.createObjectNode();
        result.put("command", "generatePerformanceReport");
        result.put("username", username);
        result.put("timestamp", timestamp);

        LocalDate reportDate = LocalDate.parse(timestamp);
        LocalDate previousMonthStart = reportDate.minusMonths(1).withDayOfMonth(1);
        LocalDate previousMonthEnd = reportDate.minusMonths(1).withDayOfMonth(
                reportDate.minusMonths(1).lengthOfMonth()
        );

        List<String> subordinates = AppState.getManagerSubordinates(username);
        subordinates.sort(String::compareTo);

        ArrayNode reportArray = mapper.createArrayNode();

        for (String devUsername : subordinates) {
            ObjectNode devNode = App.getDeveloperByUsername(devUsername);
            if (devNode == null) continue;

            List<Ticket> closedTickets = new ArrayList<>();

            for (Ticket ticket : AppState.getTickets()) {
                if (ticket.getStatus().equals("CLOSED") &&
                        ticket.getAssignedTo() != null &&
                        ticket.getAssignedTo().equals(devUsername) &&
                        ticket.getSolvedAt() != null) {

                    LocalDate solvedDate = ticket.getSolvedAt();
                    if (!solvedDate.isBefore(previousMonthStart) && !solvedDate.isAfter(previousMonthEnd)) {
                        closedTickets.add(ticket);
                    }
                }
            }

            int totalClosed = closedTickets.size();
            double averageResolutionTime = 0.0;
            double performanceScore = 0.0;

            if (totalClosed > 0) {
                int totalDays = 0;
                for (Ticket ticket : closedTickets) {
                    int days = (int) ChronoUnit.DAYS.between(ticket.getAssignedAt(), ticket.getSolvedAt()) + 1;
                    totalDays += days;
                }
                averageResolutionTime = totalDays / (double) totalClosed;

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

                    double ticketDiversityFactor = ticketDiversityFactor(bugCount, featureCount, uiCount);
                    performanceScore = Math.max(0, 0.5 * totalClosed - ticketDiversityFactor) + 5;
                } else if (seniority.equals("MID")) {
                    int highPriorityCount = 0;
                    for (Ticket ticket : closedTickets) {
                        BusinessPriority priority = getEscalatedPriority(ticket, ticket.getSolvedAt());
                        if (priority == BusinessPriority.HIGH || priority == BusinessPriority.CRITICAL) {
                            highPriorityCount++;
                        }
                    }
                    performanceScore = Math.max(0, 0.5 * totalClosed + 0.7 * highPriorityCount - 0.3 * averageResolutionTime) + 15;
                } else if (seniority.equals("SENIOR")) {
                    int highPriorityCount = 0;
                    for (Ticket ticket : closedTickets) {
                        BusinessPriority priority = getEscalatedPriority(ticket, ticket.getSolvedAt());
                        if (priority == BusinessPriority.HIGH || priority == BusinessPriority.CRITICAL) {
                            highPriorityCount++;
                        }
                    }
                    performanceScore = Math.max(0, 0.5 * totalClosed + 1.0 * highPriorityCount - 0.5 * averageResolutionTime) + 30;
                }
            }

            devNode.put("performanceScore", Math.round(performanceScore * 100.0) / 100.0);

            ObjectNode devReport = mapper.createObjectNode();
            devReport.put("username", devUsername);
            devReport.put("closedTickets", totalClosed);
            devReport.put("averageResolutionTime", Math.round(averageResolutionTime * 100.0) / 100.0);
            devReport.put("performanceScore", Math.round(performanceScore * 100.0) / 100.0);
            devReport.put("seniority", devNode.get("seniority").asText());
            reportArray.add(devReport);
        }

        result.set("report", reportArray);
        App.addOutput(result);
    }

    @Override
    public void undo() {
    }

    private static double averageResolvedTicketType(int bug, int feature, int ui) {
        return (bug + feature + ui) / 3.0;
    }

    private static double standardDeviation(int bug, int feature, int ui) {
        double mean = averageResolvedTicketType(bug, feature, ui);
        double variance = (Math.pow(bug - mean, 2) + Math.pow(feature - mean, 2) + Math.pow(ui - mean, 2)) / 3.0;
        return Math.sqrt(variance);
    }

    private static double ticketDiversityFactor(int bug, int feature, int ui) {
        double mean = averageResolvedTicketType(bug, feature, ui);
        if (mean == 0.0) {
            return 0.0;
        }
        double std = standardDeviation(bug, feature, ui);
        return std / mean;
    }

    private BusinessPriority getEscalatedPriority(Ticket ticket, LocalDate solvedDate) {
        String milestoneName = AppState.getMilestoneNameByTicket(ticket.getId());
        if (milestoneName == null) {
            return ticket.getBusinessPriority();
        }

        Milestone milestone = AppState.getMilestoneByName(milestoneName);
        if (milestone == null || milestone.isBlocked()) {
            return ticket.getBusinessPriority();
        }

        BusinessPriority priority = ticket.getBusinessPriority();
        long daysSinceMilestoneCreation = ChronoUnit.DAYS.between(
                milestone.getCreatedAt(),
                solvedDate
        );

        long daysUntilDue = ChronoUnit.DAYS.between(solvedDate, milestone.getDueDate());
        if (daysUntilDue == 1) {
            return BusinessPriority.CRITICAL;
        }

        if (daysSinceMilestoneCreation >= 3) {
            return switch (priority) {
                case LOW -> BusinessPriority.MEDIUM;
                case MEDIUM -> BusinessPriority.HIGH;
                case HIGH -> BusinessPriority.CRITICAL;
                case CRITICAL -> BusinessPriority.CRITICAL;
            };
        }

        return priority;
    }
}
