package main.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import main.enums.BusinessPriority;
import main.enums.ExpertiseArea;
import main.enums.TicketStatus;
import main.enums.Type;

public abstract class Ticket {

    protected final int id;
    protected final Type type;
    protected final String title;
    protected final BusinessPriority businessPriority;
    protected TicketStatus status;
    protected final ExpertiseArea expertiseArea;
    protected final String description;
    protected final String reportedBy;
    protected final LocalDate createdAt;
    protected LocalDate solvedAt;
    protected String assignedTo;
    protected LocalDate assignedAt;
    protected final List<ObjectNode> comments;
    protected final List<ObjectNode> history;

    /**
     * Ticket constructor
     */
    protected Ticket(final int id, final Type type, final String title,
                     final BusinessPriority businessPriority, final TicketStatus status,
                     final ExpertiseArea expertiseArea, final String description,
                     final String reportedBy, final LocalDate createdAt, final LocalDate solvedAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.businessPriority = businessPriority;
        this.status = status;
        this.expertiseArea = expertiseArea;
        this.description = description;
        this.reportedBy = reportedBy;
        this.createdAt = createdAt;
        this.solvedAt = solvedAt;
        this.assignedTo = "";
        this.assignedAt = null;
        this.comments = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    /**
     * Returns a view JSON representation of the ticket
     */
    public ObjectNode toViewJson(final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        node.put("id", id);
        node.put("type", type.name());
        node.put("title", title);
        node.put("businessPriority", businessPriority.name());
        node.put("status", status.name());
        node.put("createdAt", createdAt.toString());
        node.put("assignedAt", assignedAt == null ? "" : assignedAt.toString());
        node.put("solvedAt", solvedAt == null ? "" : solvedAt.toString());
        node.put("assignedTo", assignedTo == null ? "" : assignedTo);
        node.put("reportedBy", reportedBy);

        ArrayNode commentsArray = mapper.createArrayNode();
        for (ObjectNode comment : comments) {
            commentsArray.add(comment);
        }
        node.set("comments", commentsArray);

        return node;
    }

    /**
     * Returns a JSON representation of the ticket
     */
    public abstract ObjectNode toJson(ObjectMapper mapper);

    /**
     * Returns the reporter username
     */
    public String getReportedBy() {
        return reportedBy;
    }

    /**
     * Returns the creation date
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the ticket id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the ticket status as string
     */
    public String getStatus() {
        return status.name();
    }

    /**
     * Returns the ticket status as enum
     */
    public TicketStatus getStatusEnum() {
        return status;
    }

    /**
     * Sets the ticket status
     */
    public void setStatus(final TicketStatus status) {
        this.status = status;
    }

    /**
     * Returns the assigned user
     */
    public String getAssignedTo() {
        return assignedTo;
    }

    /**
     * Sets the assigned user
     */
    public void setAssignedTo(final String assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Returns the assigned date
     */
    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    /**
     * Returns the business priority
     */
    public BusinessPriority getBusinessPriority() {
        return businessPriority;
    }

    /**
     * Returns the expertise area
     */
    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    /**
     * Returns the ticket type
     */
    public Type getType() {
        return type;
    }

    /**
     * Adds a comment to the ticket
     */
    public void addComment(final ObjectNode comment) {
        comments.add(comment);
    }

    /**
     * Removes the last comment by the given user
     */
    public boolean elimUltComm(final String usrname) {
        for (int i = comments.size() - 1; i >= 0; i--) {
            ObjectNode comm = comments.get(i);
            if (comm.get("author").asText().equals(usrname)) {
                comments.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the ticket is anonymous
     */
    public boolean isAnonymous() {
        return reportedBy == null || reportedBy.isEmpty();
    }

    /**
     * Returns the ticket history
     */
    public List<ObjectNode> getHistory() {
        return history;
    }

    /**
     * Adds a history entry to the ticket
     */
    public void addHistoryEntry(final ObjectNode entry) {
        history.add(entry);
    }

    /**
     * Returns the ticket title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the solved date
     */
    public LocalDate getSolvedAt() {
        return solvedAt;
    }

    /**
     * Sets the solved date
     */
    public void setSolvedAt(final LocalDate solvedAt) {
        this.solvedAt = solvedAt;
    }

    /**
     * Sets the assigned date
     */
    public void setAssignedAt(final LocalDate assignedAt) {
        this.assignedAt = assignedAt;
    }

    /**
     * Returns the ticket comments
     */
    public List<ObjectNode> getComments() {
        return comments;
    }
}
