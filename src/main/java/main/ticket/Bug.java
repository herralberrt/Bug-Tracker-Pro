package main.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.enums.*;

import java.time.LocalDate;

public class Bug extends Ticket {

    private final String expectedBehavior;
    private final String actualBehavior;
    private final Frequency frequency;
    private final Severity severity;
    private final String environment;
    private final Integer errorCode;

    private Bug(Builder builder) {
        super(builder.id, Type.BUG, builder.title, builder.businessPriority, builder.status,
                builder.expertiseArea, builder.description, builder.reportedBy,
                builder.createdAt, builder.solvedAt);

        this.expectedBehavior = builder.expectedBehavior;
        this.actualBehavior = builder.actualBehavior;
        this.frequency = builder.frequency;
        this.severity = builder.severity;
        this.environment = builder.environment;
        this.errorCode = builder.errorCode;
    }

    public static class Builder {

        private int id;
        private String title;
        private BusinessPriority businessPriority;
        private TicketStatus status = TicketStatus.OPEN;
        private ExpertiseArea expertiseArea;
        private String description;
        private String reportedBy;
        private LocalDate createdAt;
        private LocalDate solvedAt;
        private String expectedBehavior;
        private String actualBehavior;
        private Frequency frequency;
        private Severity severity;
        private String environment;
        private Integer errorCode;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder businessPriority(BusinessPriority bp) {
            this.businessPriority = bp;
            return this;
        }

        public Builder expertiseArea(ExpertiseArea ea) {
            this.expertiseArea = ea;
            return this;
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder reportedBy(String reporter) {
            this.reportedBy = reporter;
            return this;
        }

        public Builder createdAt(LocalDate date) {
            this.createdAt = date;
            return this;
        }

        public Builder expectedBehavior(String v) {
            this.expectedBehavior = v;
            return this;
        }

        public Builder actualBehavior(String v) {
            this.actualBehavior = v;
            return this;
        }

        public Builder frequency(Frequency f) {
            this.frequency = f;
            return this;
        }

        public Builder severity(Severity s) {
            this.severity = s;
            return this;
        }

        public Builder environment(String env) {
            this.environment = env;
            return this;
        }

        public Builder errorCode(Integer code) {
            this.errorCode = code;
            return this;
        }

        public Bug build() {
            return new Bug(this);
        }
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public Severity getSeverity() {
        return severity;
    }

    @Override
    public ObjectNode toJson(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        node.put("id", id);
        node.put("type", type.name());
        node.put("title", title);
        node.put("businessPriority", businessPriority.name());
        node.put("status", status.name());
        node.put("createdAt", createdAt.toString());
        node.put("assignedAt", assignedAt == null ? "" : assignedAt.toString());
        node.put("reportedBy", reportedBy);

        var commentsArray = mapper.createArrayNode();
        for (var comment : comments) {
            commentsArray.add(comment);
        }
        node.set("comments", commentsArray);
        return node;
    }
}
