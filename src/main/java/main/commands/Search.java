package main.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import main.enums.ExpertiseArea;
import main.enums.Seniority;
import main.enums.TicketStatus;
import main.milestone.Milestone;
import main.ticket.Ticket;
import main.utiliz.Developer;
import main.utiliz.Manager;
import main.utiliz.Utilizator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Search implements Command {

    private final ObjectNode node;

    public Search(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        String username = node.get("username").asText();
        String timestamp = node.get("timestamp").asText();

        boolean isManager = AppState.isManager(username);
        boolean isDeveloper = AppState.isDeveloper(username);

        JsonNode filtersNode = node.get("filters");
        String searchType = filtersNode.get("searchType").asText();

        ObjectNode result = mapper.createObjectNode();
        result.put("command", "search");
        result.put("username", username);
        result.put("timestamp", timestamp);
        result.put("searchType", searchType);
        ArrayNode resultsArray = mapper.createArrayNode();

        if (searchType.equals("TICKET")) {
            List<Ticket> tickets = getTicketsForUser(username, isManager, isDeveloper);
            tickets = applyFilters(tickets, filtersNode, username, isManager, isDeveloper);

            tickets.sort((t1, t2) -> {
                int dateCompare = t1.getCreatedAt().compareTo(t2.getCreatedAt());
                if (dateCompare != 0) return dateCompare;
                return Integer.compare(t1.getId(), t2.getId());
            });

            for (Ticket ticket : tickets) {
                ObjectNode ticketNode = mapper.createObjectNode();
                ticketNode.put("id", ticket.getId());
                ticketNode.put("type", ticket.getType().toString());
                ticketNode.put("title", ticket.getTitle());
                ticketNode.put("businessPriority", ticket.getBusinessPriority().toString());
                ticketNode.put("status", ticket.getStatus());
                ticketNode.put("createdAt", ticket.getCreatedAt().toString());
                ticketNode.put("solvedAt", ticket.getSolvedAt() != null ? ticket.getSolvedAt().toString() : "");
                ticketNode.put("reportedBy", ticket.getReportedBy());

                if (filtersNode.has("keywords") && isManager) {
                    ArrayNode keywordsNode = (ArrayNode) filtersNode.get("keywords");
                    List<String> keywords = new ArrayList<>();
                    keywordsNode.forEach(n -> keywords.add(n.asText().toLowerCase()));

                    Set<String> matchingWords = findMatchingWords(ticket, keywords);
                    if (!matchingWords.isEmpty()) {
                        ArrayNode matchingWordsArray = mapper.createArrayNode();
                        matchingWords.stream().sorted().forEach(matchingWordsArray::add);
                        ticketNode.set("matchingWords", matchingWordsArray);
                    }
                }

                resultsArray.add(ticketNode);
            }
        } else if (searchType.equals("DEVELOPER")) {
            if (isManager) {
                List<Developer> developers = getDevelopersForManager(username);
                developers = applyDeveloperFilters(developers, filtersNode);
                developers.sort((d1, d2) -> d1.getUsername().compareTo(d2.getUsername()));

                for (Developer dev : developers) {
                    ObjectNode devNode = mapper.createObjectNode();
                    devNode.put("username", dev.getUsername());
                    devNode.put("expertiseArea", dev.getExpertiseArea().toString());
                    devNode.put("seniority", dev.getSeniority().toString());
                    devNode.put("performanceScore", dev.getPerformanceScore());
                    devNode.put("hireDate", dev.getHireDate());
                    resultsArray.add(devNode);
                }
            }
        }

        result.set("results", resultsArray);
        App.addOutput(result);
    }

    @Override
    public void undo() {
    }


    private List<Ticket> getTicketsForUser(String username, boolean isManager, boolean isDeveloper) {
        if (isDeveloper) {
            List<Ticket> tickets = new ArrayList<>();

            for (Milestone milestone : AppState.getMilestones()) {
                if (milestone.getAssignedDevs().contains(username)) {
                    for (int ticketId : milestone.getTickets()) {
                        Ticket ticket = AppState.getTicketById(ticketId);
                        if (ticket != null && ticket.getStatus().equals("OPEN")) {
                            tickets.add(ticket);
                        }
                    }
                }
            }
            return tickets;
        } else if (isManager) {
            return AppState.getAllTickets();
        }
        return new ArrayList<>();
    }

    private List<Ticket> applyFilters(List<Ticket> tickets, JsonNode filters, String username, boolean isManager, boolean isDeveloper) {
        List<Ticket> filtered = new ArrayList<>(tickets);

        if (filters.has("businessPriority")) {
            String priority = filters.get("businessPriority").asText();
            filtered = filtered.stream()
                    .filter(t -> t.getBusinessPriority().toString().equals(priority))
                    .collect(Collectors.toList());
        }

        if (filters.has("type")) {
            String type = filters.get("type").asText();
            filtered = filtered.stream()
                    .filter(t -> t.getType().toString().equals(type))
                    .collect(Collectors.toList());
        }

        if (filters.has("createdAt")) {
            LocalDate createdAtDate = LocalDate.parse(filters.get("createdAt").asText());
            filtered = filtered.stream()
                    .filter(t -> t.getCreatedAt().equals(createdAtDate))
                    .collect(Collectors.toList());
        }

        if (filters.has("createdBefore")) {
            LocalDate beforeDate = LocalDate.parse(filters.get("createdBefore").asText());
            filtered = filtered.stream()
                    .filter(t -> t.getCreatedAt().isBefore(beforeDate))
                    .collect(Collectors.toList());
        }

        if (filters.has("createdAfter")) {
            LocalDate afterDate = LocalDate.parse(filters.get("createdAfter").asText());
            filtered = filtered.stream()
                    .filter(t -> t.getCreatedAt().isAfter(afterDate))
                    .collect(Collectors.toList());
        }

        if (filters.has("availableForAssignment") && filters.get("availableForAssignment").asBoolean()) {
            if (isDeveloper) {
                ObjectNode devNode = App.getDeveloperByUsername(username);
                if (devNode != null) {
                    String devExpertise = devNode.get("expertiseArea").asText();
                    String devSeniority = devNode.get("seniority").asText();

                    filtered = filtered.stream()
                            .filter(t -> canDeveloperAssign(devExpertise, devSeniority, t))
                            .collect(Collectors.toList());
                }
            }
        }

        if (filters.has("keywords") && isManager) {
            ArrayNode keywordsNode = (ArrayNode) filters.get("keywords");
            List<String> keywords = new ArrayList<>();
            keywordsNode.forEach(n -> keywords.add(n.asText().toLowerCase()));

            filtered = filtered.stream()
                    .filter(t -> hasMatchingKeywords(t, keywords))
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    private boolean canDeveloperAssign(String devExpertise, String devSeniority, Ticket ticket) {

        if (!canAccessExpertiseArea(devExpertise, ticket.getExpertiseArea().toString())) {
            return false;
        }

        if (!canAccessPriority(devSeniority, ticket.getBusinessPriority().toString())) {
            return false;
        }

        if (!canAccessTicketType(devSeniority, ticket.getType().toString())) {
            return false;
        }

        return true;
    }

    private boolean canAccessExpertiseArea(String devArea, String ticketArea) {
        try {
            ExpertiseArea devExpertise = ExpertiseArea.valueOf(devArea);
            ExpertiseArea ticketExpertise = ExpertiseArea.valueOf(ticketArea);

            switch (devExpertise) {
                case FRONTEND:
                    return ticketExpertise == ExpertiseArea.FRONTEND || ticketExpertise == ExpertiseArea.DESIGN;
                case BACKEND:
                    return ticketExpertise == ExpertiseArea.BACKEND || ticketExpertise == ExpertiseArea.DB;
                case FULLSTACK:
                    return true;
                case DEVOPS:
                    return ticketExpertise == ExpertiseArea.DEVOPS;
                case DESIGN:
                    return ticketExpertise == ExpertiseArea.DESIGN || ticketExpertise == ExpertiseArea.FRONTEND;
                case DB:
                    return ticketExpertise == ExpertiseArea.DB;
                default:
                    return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean canAccessPriority(String seniorityStr, String priority) {
        Seniority seniority = Seniority.valueOf(seniorityStr);
        switch (seniority) {
            case JUNIOR:
                return priority.equals("LOW") || priority.equals("MEDIUM");
            case MID:
                return !priority.equals("CRITICAL");
            case SENIOR:
                return true;
            default:
                return false;
        }
    }

    private boolean canAccessTicketType(String seniorityStr, String type) {
        Seniority seniority = Seniority.valueOf(seniorityStr);
        switch (seniority) {
            case JUNIOR:
                return type.equals("BUG") || type.equals("UI_FEEDBACK");
            case MID:
            case SENIOR:
                return true;
            default:
                return false;
        }
    }

    private boolean hasMatchingKeywords(Ticket ticket, List<String> keywords) {
        String title = ticket.getTitle() != null ? ticket.getTitle().toLowerCase() : "";

        for (String keyword : keywords) {
            if (title.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> findMatchingWords(Ticket ticket, List<String> keywords) {
        Set<String> matchingWords = new HashSet<>();
        String title = ticket.getTitle() != null ? ticket.getTitle().toLowerCase() : "";

        String[] titleWords = title.split("\\s+");

        for (String keyword : keywords) {
            for (String word : titleWords) {
                // Remove punctuation from word
                String cleanWord = word.replaceAll("[^a-z0-9]", "");
                if (cleanWord.contains(keyword)) {
                    matchingWords.add(cleanWord);
                }
            }
        }

        return matchingWords;
    }

    private List<Developer> getDevelopersForManager(String managerUsername) {
        List<String> subordinates = AppState.getManagerSubordinates(managerUsername);
        List<Developer> developers = new ArrayList<>();

        for (String devUsername : subordinates) {
            ObjectNode devNode = App.getDeveloperByUsername(devUsername);
            if (devNode != null) {
                String hireDate = devNode.get("hireDate").asText();
                Seniority seniority = Seniority.valueOf(devNode.get("seniority").asText());
                ExpertiseArea expertiseArea = ExpertiseArea.valueOf(devNode.get("expertiseArea").asText());

                Developer dev = new Developer(devUsername, "", LocalDate.parse(hireDate), seniority, expertiseArea);
                developers.add(dev);
            }
        }

        return developers;
    }

    private List<Developer> applyDeveloperFilters(List<Developer> developers, JsonNode filters) {
        List<Developer> filtered = new ArrayList<>(developers);

        if (filters.has("expertiseArea")) {
            String area = filters.get("expertiseArea").asText();
            filtered = filtered.stream()
                    .filter(d -> d.getExpertiseArea().toString().equals(area))
                    .collect(Collectors.toList());
        }

        if (filters.has("seniority")) {
            String seniority = filters.get("seniority").asText();
            filtered = filtered.stream()
                    .filter(d -> d.getSeniority().toString().equals(seniority))
                    .collect(Collectors.toList());
        }

        if (filters.has("performanceScoreAbove")) {
            double score = filters.get("performanceScoreAbove").asDouble();
            filtered = filtered.stream()
                    .filter(d -> d.getPerformanceScore() > score)
                    .collect(Collectors.toList());
        }

        if (filters.has("performanceScoreBelow")) {
            double score = filters.get("performanceScoreBelow").asDouble();
            filtered = filtered.stream()
                    .filter(d -> d.getPerformanceScore() < score)
                    .collect(Collectors.toList());
        }

        return filtered;
    }
}
