package main.ticket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import main.enums.*;

import java.time.LocalDate;

public final class TicketFactory {

    private TicketFactory() {}

    public static Ticket create(int id, String createdAtStr, ObjectNode params) {

        Type type = Type.valueOf(params.get("type").asText());
        LocalDate createdAt = LocalDate.parse(createdAtStr);

        switch (type) {

            case BUG:
                return new Bug.Builder()
                        .id(id)
                        .title(params.get("title").asText())
                        .businessPriority(
                                BusinessPriority.valueOf(params.get("businessPriority").asText()))
                        .expertiseArea(
                                ExpertiseArea.valueOf(params.get("expertiseArea").asText()))
                        .description(
                                params.has("description") ? params.get("description").asText() : "")
                        .reportedBy(params.get("reportedBy").asText())
                        .createdAt(createdAt)
                        .expectedBehavior(params.get("expectedBehavior").asText())
                        .actualBehavior(params.get("actualBehavior").asText())
                        .frequency(
                                Frequency.valueOf(params.get("frequency").asText()))
                        .severity(
                                Severity.valueOf(params.get("severity").asText()))
                        .environment(
                                params.has("environment") ? params.get("environment").asText() : null)
                        .errorCode(
                                params.has("errorCode") ? params.get("errorCode").asInt() : null)
                        .build();

            case FEATURE_REQUEST:
                return new FeatureRequest.Builder()
                        .id(id)
                        .title(params.get("title").asText())
                        .businessPriority(
                                BusinessPriority.valueOf(params.get("businessPriority").asText()))
                        .expertiseArea(
                                ExpertiseArea.valueOf(params.get("expertiseArea").asText()))
                        .description(
                                params.has("description") ? params.get("description").asText() : "")
                        .reportedBy(params.get("reportedBy").asText())
                        .createdAt(createdAt)
                        .businessValue(
                                BusinessValue.valueOf(params.get("businessValue").asText()))
                        .customerDemand(
                                CustomerDemand.valueOf(params.get("customerDemand").asText()))
                        .build();

            case UI_FEEDBACK:
                return new UiFeedback.Builder()
                        .id(id)
                        .title(params.get("title").asText())
                        .businessPriority(
                                BusinessPriority.valueOf(params.get("businessPriority").asText()))
                        .expertiseArea(
                                ExpertiseArea.valueOf(params.get("expertiseArea").asText()))
                        .description(
                                params.has("description") ? params.get("description").asText() : "")
                        .reportedBy(params.get("reportedBy").asText())
                        .createdAt(createdAt)
                        .uiElementId(
                                params.has("uiElementId") ? params.get("uiElementId").asText() : null)
                        .businessValue(
                                BusinessValue.valueOf(params.get("businessValue").asText()))
                        .usabilityScore(params.get("usabilityScore").asInt())
                        .suggestedFix(
                                params.has("suggestedFix") ? params.get("suggestedFix").asText() : null)
                        .build();
            default:
                throw new IllegalArgumentException("Unknown ticket type");
        }
    }
}
