package main.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;
import main.AppState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import main.enums.ExpertiseArea;
import main.enums.Seniority;
import main.milestone.Milestone;
import main.ticket.Ticket;
import main.utiliz.Developer;
import main.utiliz.Utilizator;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.Comparator;

public class Search implements Command {

    private final ObjectNode node;

    /**
     * Constructs a Search command
     */
    public Search(final ObjectNode node) {
        this.node = node;
    }

    /**
     * Executes the search command
     */
    @Override
    public final void execute() {
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
            tickets.sort(Comparator.comparing(
                    Ticket::getCreatedAt).thenComparingInt(Ticket::getId));

            for (Ticket ticket : tickets) {
                ObjectNode ticketNode = mapper.createObjectNode();
                ticketNode.put("id", ticket.getId());
                ticketNode.put("type", ticket.getType().toString());
                ticketNode.put("title", ticket.getTitle());
                ticketNode.put("businessPriority", ticket.getBusinessPriority().toString());
                ticketNode.put("status", ticket.getStatus());
                ticketNode.put("createdAt", ticket.getCreatedAt().toString());
                ticketNode.put("solvedAt", ticket.getSolvedAt()
                        != null ? ticket.getSolvedAt().toString() : "");
                ticketNode.put("reportedBy", ticket.getReportedBy());

                if (isManager) {
                    ArrayNode matchingWordsArray = mapper.createArrayNode();
                    if (filtersNode.has("keywords")) {
                        ArrayNode keywordsNode = (ArrayNode) filtersNode.get("keywords");
                        List<String> keywords = new ArrayList<>();
                        keywordsNode.forEach(n -> keywords.add(n.asText().toLowerCase()));

                        Set<String> matchingWords = findWords(ticket, keywords);
                        matchingWords.stream().sorted().forEach(matchingWordsArray::add);
                    }
                    ticketNode.set("matchingWords", matchingWordsArray);
                }

                resultsArray.add(ticketNode);
            }
        } else if (searchType.equals("DEVELOPER")) {
            if (isManager) {
                List<Developer> developers = getDev(username);
                developers = devFil(developers, filtersNode);
                developers.sort(Comparator.comparing(Developer::getUsername));

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

    /**
     * Undo operation is not implemented for this command
     */
    @Override
    public void undo() {
    }


    /**
     * Returns the list of tickets accessible to the user based on their role
     */
    private List<Ticket> getTicketsForUser(final String username,
                                           final boolean isManager,
                                           final boolean isDeveloper) {
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

    /**
     * Applies the provided filters to the list of tickets
     */
    private List<Ticket> applyFilters(final List<Ticket> tickets,
                                      final JsonNode filters,
                                      final String username,
                                      final boolean isManager,
                                      final boolean isDeveloper) {
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

        if (filters.has("availableForAssignment")
                && filters.get("availableForAssignment").asBoolean()) {
            if (isDeveloper) {
                ObjectNode devNode = App.getDeveloperByUsername(username);
                if (devNode != null) {
                    String devExpertise = devNode.get("expertiseArea").asText();
                    String devSeniority = devNode.get("seniority").asText();

                    filtered = filtered.stream()
                            .filter(t -> devAssign(devExpertise, devSeniority, t))
                            .collect(Collectors.toList());
                }
            }
        }

        if (filters.has("keywords") && isManager) {
            ArrayNode keywordsNode = (ArrayNode) filters.get("keywords");
            List<String> keywords = new ArrayList<>();
            keywordsNode.forEach(n -> keywords.add(n.asText().toLowerCase()));

            filtered = filtered.stream()
                    .filter(t -> hasWords(t, keywords))
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    /**
     * Checks if a developer can be assigned to a ticket based on expertise and seniority
     */
    private boolean devAssign(final String devExpertise,
                              final String devSeniority,
                              final Ticket ticket) {
        if (!accArea(devExpertise, ticket.getExpertiseArea().toString())) {
            return false;
        }

        if (!accPr(devSeniority, ticket.getBusinessPriority().toString())) {
            return false;
        }

        if (!accTicket(devSeniority, ticket.getType().toString())) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a developer's expertise area matches the ticket's required area
     */
    private boolean accArea(final String devArea,
                            final String ticketArea) {
        try {
            ExpertiseArea expDev = ExpertiseArea.valueOf(devArea);
            ExpertiseArea expTick = ExpertiseArea.valueOf(ticketArea);

            switch (expDev) {
                case FRONTEND:
                    return expTick == ExpertiseArea.FRONTEND
                            || expTick == ExpertiseArea.DESIGN;

                case BACKEND:
                    return expTick == ExpertiseArea.BACKEND
                            || expTick == ExpertiseArea.DB;

                case FULLSTACK:
                    return true;

                case DEVOPS:
                    return expTick == ExpertiseArea.DEVOPS;

                case DESIGN:
                    return expTick == ExpertiseArea.DESIGN
                            || expTick == ExpertiseArea.FRONTEND;

                case DB:
                    return expTick == ExpertiseArea.DB;

                default:
                    return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if a developer's seniority allows access to a ticket's priority.
     */
    private boolean accPr(final String seniorityStr,
                          final String priority) {
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

    /**
     * Checks if a developer's seniority allows access to a ticket's type
     */
    private boolean accTicket(final String seniorityStr,
                              final String type) {
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

    /**
     * Checks if a ticket's title contains any of the provided keywords
     */
    private boolean hasWords(final Ticket ticket,
                             final List<String> keywords) {
        String title = ticket.getTitle() != null ? ticket.getTitle().toLowerCase() : "";

        for (String keyword : keywords) {
            if (title.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds and returns the set of matching words from a ticket's title based on keywords
     */
    private Set<String> findWords(final Ticket ticket,
                                  final List<String> keywords) {

        Set<String> match = new HashSet<>();
        String title = ticket.getTitle() != null ? ticket.getTitle().toLowerCase() : "";
        String[] titWords = title.split(" ");

        for (String keyword : keywords) {
            for (String word : titWords) {
                StringBuilder sb = new StringBuilder();
                for (char c : word.toCharArray()) {
                    if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                        sb.append(c);
                    }
                }
                String cln = sb.toString();
                if (cln.contains(keyword)) {
                    match.add(cln);
                }
            }
        }

        return match;
    }

    /**
     * Returns the list of developers managed by the given manager
     */
    private List<Developer> getDev(final String managerUsername) {
        List<String> subordinates = AppState.getManSubord(managerUsername);
        List<Developer> developers = new ArrayList<>();

        for (String devUsername : subordinates) {
            ObjectNode devNode = App.getDeveloperByUsername(devUsername);
            if (devNode != null) {
                Utilizator utiliz = main.utiliz.UtilizFactory.create(devNode);
                if (utiliz instanceof Developer) {
                    Developer dev = (Developer) utiliz;
                    if (devNode.has("performanceScore")) {
                        dev.setPerformanceScore(devNode.get("performanceScore").asDouble());
                    }
                    developers.add(dev);
                }
            }
        }

        return developers;
    }

    /**
     * Applies the provided filters to the list of developers
     */
    private List<Developer> devFil(final List<Developer> developers,
                                   final JsonNode filters) {

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
                    .filter(d -> d.getPerformanceScore() >= score)
                    .collect(Collectors.toList());
        }

        if (filters.has("performanceScoreBelow")) {
            double score = filters.get("performanceScoreBelow").asDouble();
            filtered = filtered.stream()
                    .filter(d -> d.getPerformanceScore() <= score)
                    .collect(Collectors.toList());
        }
        return filtered;
    }
}
