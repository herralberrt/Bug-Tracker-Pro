package main.ticket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;

import main.enums.*;

public final class TicketFactory {

    private TicketFactory() {
    }

    public static Ticket createTicket(
            ObjectNode params,
            int id,
            LocalDate createdAt
    ) {
        Type type = Type.valueOf(params.get("type").asText());

        switch (type) {
            case BUG:
                return createBug(params, id, createdAt);

            case FEATURE_REQUEST:
                return createFeatureRequest(params, id, createdAt);

            case UI_FEEDBACK:
                return createUiFeedback(params, id, createdAt);

            default:
                throw new IllegalArgumentException("Unknown ticket type");
        }
    }

    private static Bug createBug(ObjectNode p, int id, LocalDate createdAt) {
        Bug.Builder builder = new Bug.Builder()
                .id(id)
                .title(p.get("title").asText())
                .businessPriority(BusinessPriority.valueOf(p.get("businessPriority").asText()))
                .expertiseArea(ExpertiseArea.valueOf(p.get("expertiseArea").asText()))
                .reportedBy(p.get("reportedBy").asText())
                .createdAt(createdAt)
                .expectedBehavior(p.get("expectedBehavior").asText())
                .actualBehavior(p.get("actualBehavior").asText())
                .frequency(Frequency.valueOf(p.get("frequency").asText()))
                .severity(Severity.valueOf(p.get("severity").asText()));

        if (p.has("description")) {
            builder.description(p.get("description").asText());
        }

        if (p.has("environment")) {
            builder.environment(p.get("environment").asText());
        }

        if (p.has("errorCode")) {
            builder.errorCode(p.get("errorCode").asInt());
        }

        return builder.build();
    }

    private static FeatureRequest createFeatureRequest(
            ObjectNode p,
            int id,
            LocalDate createdAt
    ) {
        FeatureRequest.Builder builder = new FeatureRequest.Builder()
                .id(id)
                .title(p.get("title").asText())
                .businessPriority(BusinessPriority.valueOf(p.get("businessPriority").asText()))
                .expertiseArea(ExpertiseArea.valueOf(p.get("expertiseArea").asText()))
                .reportedBy(p.get("reportedBy").asText())
                .createdAt(createdAt)
                .businessValue(BusinessValue.valueOf(p.get("businessValue").asText()))
                .customerDemand(CustomerDemand.valueOf(p.get("customerDemand").asText()));

        if (p.has("description")) {
            builder.description(p.get("description").asText());
        }

        return builder.build();
    }

    private static UiFeedback createUiFeedback(
            ObjectNode p,
            int id,
            LocalDate createdAt
    ) {
        UiFeedback.Builder builder = new UiFeedback.Builder()
                .id(id)
                .title(p.get("title").asText())
                .businessPriority(BusinessPriority.valueOf(p.get("businessPriority").asText()))
                .expertiseArea(ExpertiseArea.valueOf(p.get("expertiseArea").asText()))
                .reportedBy(p.get("reportedBy").asText())
                .createdAt(createdAt)
                .businessValue(BusinessValue.valueOf(p.get("businessValue").asText()))
                .usabilityScore(p.get("usabilityScore").asInt());

        if (p.has("description")) {
            builder.description(p.get("description").asText());
        }

        if (p.has("uiElementId")) {
            builder.uiElementId(p.get("uiElementId").asText());
        }

        if (p.has("screenshotUrl")) {
            builder.screenshotUrl(p.get("screenshotUrl").asText());
        }

        if (p.has("suggestedFix")) {
            builder.suggestedFix(p.get("suggestedFix").asText());
        }

        return builder.build();
    }
}
