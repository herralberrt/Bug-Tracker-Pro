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

    private final String name;
    private final List<String> blockingFor;
    private final LocalDate dueDate;
    private final LocalDate createdAt;
    private final List<Integer> tickets;
    private final List<String> assignedDevs;
    private final String createdBy;
    private final List<Observer> observers = new ArrayList<>();
    private Integer frozenDaysUntilDue = null;
    private Integer frozenOverdueBy = null;

    public Milestone(String name, List<String> blockingFor, LocalDate dueDate,
                     LocalDate createdAt, List<Integer> tickets,
                     List<String> assignedDevs, String createdBy) {

        this.name = name;
        this.blockingFor = blockingFor;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.tickets = tickets;
        this.assignedDevs = assignedDevs;
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public List<String> getBlockingFor() {
        return blockingFor;
    }

    public List<Integer> getTickets() {
        return tickets;
    }

    public List<String> getAssignedDevs() {
        return assignedDevs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

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

    public boolean containsAnyOpenTickets() {
        for (Integer ticketId : tickets) {
            Ticket ticket = AppState.getTicketById(ticketId);
            if (ticket != null && !ticket.getStatus().equals("CLOSED")) {
                return true;
            }
        }
        return false;
    }

    public int calculateDaysRemainingUntilDue(LocalDate currentDate) {
        if (frozenDaysUntilDue != null) {
            return frozenDaysUntilDue;
        }

        if (currentDate.isAfter(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(currentDate, dueDate) + 1;
    }

    public int calculateDaysOverdue(LocalDate currentDate) {
        if (frozenOverdueBy != null) {
            return frozenOverdueBy;
        }

        if (currentDate.isBefore(dueDate) || currentDate.isEqual(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, currentDate) + 1;
    }

    public String getStatus() {
        for (Integer ticketId : tickets) {
            Ticket ticket = AppState.getTicketById(ticketId);
            if (ticket != null && !ticket.getStatus().equals("CLOSED")) {
                return "ACTIVE";
            }
        }
        return "COMPLETED";
    }

    public void freezeMetricsIfCompleted(LocalDate currentDate) {
        if (getStatus().equals("COMPLETED") && frozenDaysUntilDue == null) {
            frozenDaysUntilDue = calculateDaysRemainingUntilDue(currentDate);
            frozenOverdueBy = calculateDaysOverdue(currentDate);
        }
    }

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

    public double calculateCompletionPercentage() {
        if (tickets.isEmpty()) {
            return 0.0;
        }
        int closedCount = retrieveAllClosedTicketIds().size();
        double fraction = closedCount * 1.0 / tickets.size();
        return Math.round(fraction * 100.0) / 100.0;
    }

    public ObjectNode toJson(ObjectMapper mapper, LocalDate currentDate) {
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

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    @Override
    public void attach(Observer o) {
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer o : observers) {
            o.update(message);
        }
    }
}
