package main.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import main.enums.BusinessValue;
import main.enums.BusinessPriority;
import main.enums.TicketStatus;
import main.enums.ExpertiseArea;
import main.enums.CustomerDemand;
import main.enums.Type;
import java.time.LocalDate;

public final class FeatureRequest extends Ticket {

    private final BusinessValue businessValue;
    private final CustomerDemand customerDemand;

    private FeatureRequest(final Builder builder) {
        super(builder.id, Type.FEATURE_REQUEST, builder.title, builder.businessPriority,
                builder.status, builder.expertiseArea, builder.description,
                builder.reportedBy, builder.createdAt, builder.solvedAt);

        this.businessValue = builder.businessValue;
        this.customerDemand = builder.customerDemand;
    }

    /**
     * Builder for FeatureRequest objects
     */
    public static final class Builder {

        private int id;
        private String title;
        private BusinessPriority businessPriority;
        private TicketStatus status = TicketStatus.OPEN;
        private ExpertiseArea expertiseArea;
        private String description;
        private String reportedBy;
        private BusinessValue businessValue;
        private CustomerDemand customerDemand;
        private LocalDate createdAt;
        private LocalDate solvedAt;

        /**
         * Sets the feature request id
         */
        public Builder id(final int idParam) {
            this.id = idParam;
            return this;
        }

        /**
         * Sets the feature request title
         */
        public Builder title(final String titleParam) {
            this.title = titleParam;
            return this;
        }

        /**
         * Sets the business priority
         */
        public Builder businessPriority(final BusinessPriority businessPriorityParam) {
            this.businessPriority = businessPriorityParam;
            return this;
        }

        /**
         * Sets the expertise area
         */
        public Builder expertiseArea(final ExpertiseArea expertiseAreaParam) {
            this.expertiseArea = expertiseAreaParam;
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
        public Builder createdAt(final LocalDate dateParam) {
            this.createdAt = dateParam;
            return this;
        }

        /**
         * Sets the business value
         */
        public Builder businessValue(final BusinessValue businessValueParam) {
            this.businessValue = businessValueParam;
            return this;
        }

        /**
         * Sets the customer demand
         */
        public Builder customerDemand(final CustomerDemand customerDemandParam) {
            this.customerDemand = customerDemandParam;
            return this;
        }

        /**
         * Builds the FeatureRequest object
         */
        public FeatureRequest build() {
            return new FeatureRequest(this);
        }
    }

    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    public CustomerDemand getCustomerDemand() {
        return customerDemand;
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
