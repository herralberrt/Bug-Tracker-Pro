package main.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import main.enums.BusinessPriority;
import main.enums.ExpertiseArea;
import main.enums.Frequency;
import main.enums.Severity;
import main.enums.TicketStatus;
import main.enums.Type;
import java.time.LocalDate;

public final class Bug extends Ticket {

    private final String expectedBehavior;
    private final String actualBehavior;
    private final Frequency frequency;
    private final Severity severity;
    private final String environment;
    private final Integer errorCode;

    private Bug(final Builder builder) {
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

    /**
     * Builder for Bug objects
     */
    public static final class Builder {

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

        /**
         * Sets the bug id
         */
        public Builder id(final int bugId) {
            this.id = bugId;
            return this;
        }

        /**
         * Sets the bug title
         */
        public Builder title(final String titleParam) {
            this.title = titleParam;
            return this;
        }

        /**
         * Sets the business priority
         */
        public Builder businessPriority(final BusinessPriority bPriority) {
            this.businessPriority = bPriority;
            return this;
        }

        /**
         * Sets the expertise area
         */
        public Builder expertiseArea(final ExpertiseArea expArea) {
            this.expertiseArea = expArea;
            return this;
        }

        /**
         * Sets the description
         */
        public Builder description(final String desc) {
            this.description = desc;
            return this;
        }

        /**
         * Sets the reporter
         */
        public Builder reportedBy(final String reporter) {
            this.reportedBy = reporter;
            return this;
        }

        /**
         * Sets the creation date
         */
        public Builder createdAt(final LocalDate date) {
            this.createdAt = date;
            return this;
        }

        /**
         * Sets the expected behavior
         */
        public Builder expectedBehavior(final String expBehavior) {
            this.expectedBehavior = expBehavior;
            return this;
        }

        /**
         * Sets the actual behavior
         */
        public Builder actualBehavior(final String actBehavior) {
            this.actualBehavior = actBehavior;
            return this;
        }

        /**
         * Sets the frequency
         */
        public Builder frequency(final Frequency freq) {
            this.frequency = freq;
            return this;
        }

        /**
         * Sets the severity
         */
        public Builder severity(final Severity sev) {
            this.severity = sev;
            return this;
        }

        /**
         * Sets the environment
         */
        public Builder environment(final String env) {
            this.environment = env;
            return this;
        }

        /**
         * Sets the error code
         */
        public Builder errorCode(final Integer code) {
            this.errorCode = code;
            return this;
        }

        /**
         * Builds the Bug object
         */
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
    public ObjectNode toJson(final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        node.put("id", id);
        node.put("type", type.name());
        node.put("title", title);
        node.put("businessPriority", businessPriority.name());
        node.put("status", status.name());
        node.put("createdAt", createdAt.toString());
        node.put("assignedAt", assignedAt == null ? "" : assignedAt.toString());
        node.put("reportedBy", reportedBy);

        ArrayNode arrComm = mapper.createArrayNode();
        for (ObjectNode comment : comments) {
            arrComm.add(comment);
        }
        node.set("comments", arrComm);
        return node;
    }
}
