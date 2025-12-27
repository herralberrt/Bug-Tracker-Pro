package main.ticket;

import java.time.LocalDate;
import main.enums.*;

public abstract class Ticket {

    protected final int id;
    protected final Type type;
    protected final String title;
    protected final BusinessPriority businessPriority;
    protected final TicketStatus status;
    protected final ExpertiseArea expertiseArea;
    protected final String description; // optional
    protected final String reportedBy;
    protected final LocalDate createdAt;
    protected final LocalDate solvedAt;

    protected Ticket(int id, Type type, String title, BusinessPriority businessPriority,
                     TicketStatus status, ExpertiseArea expertiseArea, String description,
                     String reportedBy, LocalDate createdAt, LocalDate solvedAt) {
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
}
