package main.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import main.enums.BusinessPriority;
import main.enums.BusinessValue;
import main.enums.ExpertiseArea;
import main.enums.TicketStatus;
import main.enums.Type;

import java.time.LocalDate;

public final class UiFeedback extends Ticket {

    private final String uiElementId;
    private final BusinessValue businessValue;
    private final int usabilityScore;
    private final String screenshotUrl;
    private final String suggestedFix;

    private UiFeedback(final Builder builder) {
        super(builder.id, Type.UI_FEEDBACK, builder.title, builder.businessPriority,
                builder.status, builder.expertiseArea, builder.description,
                builder.reportedBy, builder.createdAt, builder.solvedAt);

        this.uiElementId = builder.uiElementId;
        this.businessValue = builder.businessValue;
        this.usabilityScore = builder.usabilityScore;
        this.screenshotUrl = builder.screenshotUrl;
        this.suggestedFix = builder.suggestedFix;
    }

    /**
     * Builder for UiFeedback objects
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
        private String screenshotUrl;
        private String suggestedFix;
        private String uiElementId;
        private BusinessValue businessValue;
        private int usabilityScore;

        /**
         * Sets the ticket id
         */
        public Builder id(final int ticketId) {
            this.id = ticketId;
            return this;
        }

        /**
         * Sets the ticket title
         */
        public Builder title(final String ticketTitle) {
            this.title = ticketTitle;
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
         * Sets the UI element id
         */
        public Builder uiElementId(final String elementId) {
            this.uiElementId = elementId;
            return this;
        }

        /**
         * Sets the business value
         */
        public Builder businessValue(final BusinessValue bValue) {
            this.businessValue = bValue;
            return this;
        }

        /**
         * Sets the usability score
         */
        public Builder usabilityScore(final int score) {
            this.usabilityScore = score;
            return this;
        }

        /**
         * Sets the screenshot URL
         */
        public Builder screenshotUrl(final String url) {
            this.screenshotUrl = url;
            return this;
        }

        /**
         * Sets the suggested fix
         */
        public Builder suggestedFix(final String fix) {
            this.suggestedFix = fix;
            return this;
        }

        /**
         * Builds the UiFeedback object
         */
        public UiFeedback build() {
            return new UiFeedback(this);
        }
    }

    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    public int getUsabilityScore() {
        return usabilityScore;
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
