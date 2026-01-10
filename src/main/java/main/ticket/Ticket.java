package main.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.enums.*;

import java.time.LocalDate;

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
    }

    public ObjectNode toViewJson(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        node.put("id", id);
        node.put("type", type.name());
        node.put("title", title);
        node.put("businessPriority", businessPriority.name());
        node.put("status", status.name());
        node.put("createdAt", createdAt.toString());
        node.put("assignedAt", "");
        node.put("solvedAt", solvedAt == null ? "" : solvedAt.toString());
        node.put("assignedTo", "");
        node.put("reportedBy", reportedBy);
        node.set("comments", mapper.createArrayNode());

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
}
