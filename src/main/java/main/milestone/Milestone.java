package main.milestone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.AppState;
import main.ticket.Ticket;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

public class Milestone implements Subject {
    private static final double PERCENTAGE_FACTOR = 100.0;

    private final String name;
    private final List<String> blockingFor;
    private final LocalDate dueDate;
    private final LocalDate createdAt;
    private final List<Integer> tickets;
    private final List<String> assignedDevs;
    private final List<Observer> observers = new ArrayList<>();
    private final String createdBy;
    private Integer frozenDaysUntilDue = null;
    private Integer frozenOverdueBy = null;

    /**
     * Milestone constructor
     */
    public Milestone(final String name, final List<String> blockingFor, final LocalDate dueDate,
                     final LocalDate createdAt, final List<Integer> tickets,
                     final List<String> assignedDevs, final String createdBy) {

        this.name = name;
        this.blockingFor = blockingFor;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.tickets = tickets;
        this.assignedDevs = assignedDevs;
        this.createdBy = createdBy;
        syncObserversWithAssignedDevs();
    }

    /**
     * Returns the milestone name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the blockingFor list
     */
    public List<String> getBlockingFor() {
        return blockingFor;
    }

    /**
     * Returns the tickets list
     */
    public List<Integer> getTickets() {
        return tickets;
    }

    /**
     * Returns the assigned developers list
     */
    public List<String> getAssignedDevs() {
        return assignedDevs;
    }

    /**
     * Returns the creator username
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Returns the due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Returns true if the milestone is blocked
     */
    public boolean isBlocked() {
        for (Milestone m : AppState.getMilestones()) {
            if (m.blockingFor.contains(this.name)) {
                if (m.containsAnyOpenTickets()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if milestone contains any open tickets
     */
    public boolean containsAnyOpenTickets() {
        for (Integer ticketId : tickets) {
            Ticket ticket = AppState.getTicketById(ticketId);
            if (ticket != null && !ticket.getStatus().equals("CLOSED")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates days remaining until due date
     */
    public int calculateDaysRemainingUntilDue(final LocalDate currentDate) {
        if (frozenDaysUntilDue != null) {
            return frozenDaysUntilDue;
        }

        if (currentDate.isAfter(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(currentDate, dueDate) + 1;
    }

    /**
     * Calculates days overdue
     */
    public int calculateDaysOverdue(final LocalDate currentDate) {
        if (frozenOverdueBy != null) {
            return frozenOverdueBy;
        }

        if (currentDate.isBefore(dueDate) || currentDate.isEqual(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, currentDate) + 1;
    }

    /**
     * Returns the milestone status
     */
    public String getStatus() {
        for (Integer ticketId : tickets) {
            Ticket ticket = AppState.getTicketById(ticketId);
            if (ticket != null && !ticket.getStatus().equals("CLOSED")) {
                return "ACTIVE";
            }
        }
        return "COMPLETED";
    }

    /**
     * Freezes metrics if milestone is completed
     */
    public void freezeMetricsIfCompleted(final LocalDate currentDate) {
        if (getStatus().equals("COMPLETED") && frozenDaysUntilDue == null) {
            frozenDaysUntilDue = calculateDaysRemainingUntilDue(currentDate);
            frozenOverdueBy = calculateDaysOverdue(currentDate);
        }
    }

    /**
     * Returns all open ticket ids
     */
    public List<Integer> retrieveAllOpenTicketIds() {
        List<Integer> openTickets = new ArrayList<>();
        for (Integer ticketId : tickets) {
            Ticket ticket = AppState.getTicketById(ticketId);
            if (ticket != null && !ticket.getStatus().equals("CLOSED")) {
                openTickets.add(ticketId);
            }
        }
        return openTickets;
    }

    /**
     * Returns all closed ticket ids
     */
    public List<Integer> retrieveAllClosedTicketIds() {
        List<Integer> closedTickets = new ArrayList<>();
        for (Integer ticketId : tickets) {
            Ticket ticket = AppState.getTicketById(ticketId);
            if (ticket != null && ticket.getStatus().equals("CLOSED")) {
                closedTickets.add(ticketId);
            }
        }
        return closedTickets;
    }

    /**
     * Calculates the completion percentage
     */
    public double calculateCompletionPercentage() {
        if (tickets.isEmpty()) {
            return 0.0;
        }
        int closedCount = retrieveAllClosedTicketIds().size();
        double fraction = closedCount * 1.0 / tickets.size();
        return Math.round(fraction * PERCENTAGE_FACTOR) / PERCENTAGE_FACTOR;
    }

    /**
     * Returns a JSON representation of the milestone
     */
    public ObjectNode toJson(final ObjectMapper mapper, final LocalDate currentDate) {
        ObjectNode node = mapper.createObjectNode();
        node.put("name", name);
        ArrayNode blockingForArray = mapper.createArrayNode();

        for (String blocked : blockingFor) {
            blockingForArray.add(blocked);
        }

        node.set("blockingFor", blockingForArray);
        node.put("dueDate", dueDate.toString());
        node.put("createdAt", createdAt.toString());
        ArrayNode ticketsArray = mapper.createArrayNode();

        for (Integer ticketId : tickets) {
            ticketsArray.add(ticketId);
        }

        node.set("tickets", ticketsArray);
        ArrayNode devsArray = mapper.createArrayNode();

        for (String dev : assignedDevs) {
            devsArray.add(dev);
        }

        node.set("assignedDevs", devsArray);
        node.put("createdBy", createdBy);
        node.put("status", getStatus());
        node.put("isBlocked", isBlocked());
        node.put("daysUntilDue", calculateDaysRemainingUntilDue(currentDate));
        node.put("overdueBy", calculateDaysOverdue(currentDate));
        ArrayNode openTicketsArray = mapper.createArrayNode();

        for (Integer ticketId : retrieveAllOpenTicketIds()) {
            openTicketsArray.add(ticketId);
        }

        node.set("openTickets", openTicketsArray);
        ArrayNode closedTicketsArray = mapper.createArrayNode();

        for (Integer ticketId : retrieveAllClosedTicketIds()) {
            closedTicketsArray.add(ticketId);
        }
        node.set("closedTickets", closedTicketsArray);
        node.put("completionPercentage", calculateCompletionPercentage());

        List<ObjectNode> repartitionList = new ArrayList<>();
        for (String dev : assignedDevs) {
            ObjectNode devNode = mapper.createObjectNode();
            devNode.put("developer", dev);
            ArrayNode devTickets = mapper.createArrayNode();
            for (int ticketId : tickets) {
                main.ticket.Ticket ticket = AppState.getTicketById(ticketId);
                if (ticket != null && dev.equals(ticket.getAssignedTo())) {
                    devTickets.add(ticketId);
                }
            }
            devNode.set("assignedTickets", devTickets);
            repartitionList.add(devNode);
        }

        repartitionList.sort((d1, d2) -> {
            int size1 = d1.get("assignedTickets").size();
            int size2 = d2.get("assignedTickets").size();
            if (size1 != size2) {
                return Integer.compare(size1, size2);
            }
            return d1.get("developer").asText().compareTo(d2.get("developer").asText());
        });

        ArrayNode repartitionArray = mapper.createArrayNode();
        for (ObjectNode devNode : repartitionList) {
            repartitionArray.add(devNode);
        }
        node.set("repartition", repartitionArray);

        return node;
    }

    /**
     * Returns the creation date
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }


    /**
     * Attaches an observer
     */
    @Override
    public void addObs(final Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    /**
     * Attaches a developer as observer by username
     */
    public void attachDeveloperByUsername(final String username) {
        main.utiliz.Developer dev = main.AppState.getDeveloperInstanceByUsername(username);
        if (dev != null) {
            addObs(dev);
        }
    }

    /**
     * Syncs observers with assigned developers
     */
    public void syncObserversWithAssignedDevs() {
        observers.clear();
        for (String username : assignedDevs) {
            attachDeveloperByUsername(username);
        }
    }

    /**
     * Notifies all observers
     */
    @Override
    public void notifObs(final String message) {
        for (Observer o : observers) {
            o.update(message);
        }
    }
}
