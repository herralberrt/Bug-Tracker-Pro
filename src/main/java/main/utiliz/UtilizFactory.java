package main.utiliz;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import main.enums.ExpertiseArea;
import main.enums.Seniority;

public final class UtilizFactory {

    protected UtilizFactory() {

    }

    /**
     * Creates a user instance from a JSON node
     */
    public static Utilizator create(final ObjectNode node) {
        String rol = node.get("role").asText();

        switch (rol) {
            case "REPORTER":
                return new Reporter(node.get("username").asText(), node.get("email").asText());

            case "DEVELOPER":
                return new Developer(node.get("username").asText(), node.get("email").asText(),
                        LocalDate.parse(node.get("hireDate").asText()),
                        Seniority.valueOf(node.get("seniority").asText()),
                        ExpertiseArea.valueOf(node.get("expertiseArea").asText()));

            case "MANAGER":
                List<String> subords = new ArrayList<>();
                node.get("subordinates").forEach(n -> subords.add(n.asText()));

                return new Manager(node.get("username").asText(), node.get("email").asText(),
                        LocalDate.parse(node.get("hireDate").asText()), subords);
        }
        return null;
    }
}
