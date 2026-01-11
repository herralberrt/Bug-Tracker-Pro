package main.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.enums.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    protected Ticket(
            int id,
            Type type,
            String title,
            BusinessPriority businessPriority,
            TicketStatus status,
            ExpertiseArea expertiseArea,
            String description,
            String reportedBy,
            LocalDate createdAt,
            LocalDate solvedAt
    ) {
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

    public ObjectNode toViewJson(ObjectMapper mapper) {
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

    public abstract ObjectNode toJson(ObjectMapper mapper);

    public String getReportedBy() {
        return reportedBy;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status.name();
    }

    public TicketStatus getStatusEnum() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDate assignedAt) {
        this.assignedAt = assignedAt;
    }

    public BusinessPriority getBusinessPriority() {
        return businessPriority;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public Type getType() {
        return type;
    }

    public void addComment(ObjectNode comment) {
        comments.add(comment);
    }

    public boolean removeLastCommentByUser(String username) {
        for (int i = comments.size() - 1; i >= 0; i--) {
            ObjectNode comment = comments.get(i);
            if (comment.get("author").asText().equals(username)) {
                comments.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean isAnonymous() {
        return reportedBy == null || reportedBy.isEmpty();
    }

    public void addHistoryEntry(ObjectNode entry) {
        history.add(entry);
    }

    public List<ObjectNode> getHistory() {
        return history;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(LocalDate solvedAt) {
        this.solvedAt = solvedAt;
    }

    public List<ObjectNode> getComments() {
        return comments;
    }
}
