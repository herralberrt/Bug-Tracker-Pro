package main.ticket;

import java.time.LocalDate;
import main.enums.*;

public class FeatureRequest extends Ticket {

    private final BusinessValue businessValue;
    private final CustomerDemand customerDemand;

    private FeatureRequest(Builder builder) {
        super(builder.id, Type.FEATURE_REQUEST, builder.title, builder.businessPriority,
                builder.status, builder.expertiseArea, builder.description, builder.reportedBy,
                builder.createdAt, builder.solvedAt);

        this.businessValue = builder.businessValue;
        this.customerDemand = builder.customerDemand;
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

        private BusinessValue businessValue;
        private CustomerDemand customerDemand;

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

        public Builder businessValue(BusinessValue bv) {
            this.businessValue = bv;
            return this;
        }

        public Builder customerDemand(CustomerDemand cd) {
            this.customerDemand = cd;
            return this;
        }

        public FeatureRequest build() {
            return new FeatureRequest(this);
        }
    }
}
