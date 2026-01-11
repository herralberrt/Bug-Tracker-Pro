package main.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.enums.*;

import java.time.LocalDate;

public class UiFeedback extends Ticket {

    private final String uiElementId;
    private final BusinessValue businessValue;
    private final int usabilityScore;
    private final String screenshotUrl;
    private final String suggestedFix;

    private UiFeedback(Builder builder) {
        super(builder.id, Type.UI_FEEDBACK, builder.title, builder.businessPriority,
                builder.status, builder.expertiseArea, builder.description,
                builder.reportedBy, builder.createdAt, builder.solvedAt);

        this.uiElementId = builder.uiElementId;
        this.businessValue = builder.businessValue;
        this.usabilityScore = builder.usabilityScore;
        this.screenshotUrl = builder.screenshotUrl;
        this.suggestedFix = builder.suggestedFix;
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

        private String uiElementId;
        private BusinessValue businessValue;
        private int usabilityScore;
        private String screenshotUrl;
        private String suggestedFix;

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

        public Builder uiElementId(String id) {
            this.uiElementId = id;
            return this;
        }

        public Builder businessValue(BusinessValue bv) {
            this.businessValue = bv;
            return this;
        }

        public Builder usabilityScore(int score) {
            this.usabilityScore = score;
            return this;
        }

        public Builder screenshotUrl(String url) {
            this.screenshotUrl = url;
            return this;
        }

        public Builder suggestedFix(String fix) {
            this.suggestedFix = fix;
            return this;
        }

        public UiFeedback build() {
            return new UiFeedback(this);
        }
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
