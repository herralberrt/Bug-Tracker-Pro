package main.utiliz;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import main.enums.ExpertiseArea;
import main.enums.Seniority;

public final class UtilizFactory {

    private UtilizFactory() {
    }

    public static Utilizator create(ObjectNode node) {
        String role = node.get("role").asText();

        switch (role) {
            case "REPORTER":
                return new Reporter(node.get("username").asText(), node.get("email").asText());

            case "DEVELOPER":
                return new Developer(node.get("username").asText(), node.get("email").asText(),
                        LocalDate.parse(node.get("hireDate").asText()),
                        Seniority.valueOf(node.get("seniority").asText()),
                        ExpertiseArea.valueOf(node.get("expertiseArea").asText())
                );

            case "MANAGER":
                List<String> subs = new ArrayList<>();
                node.get("subordinates").forEach(n -> subs.add(n.asText()));

                return new Manager(node.get("username").asText(), node.get("email").asText(),
                        LocalDate.parse(node.get("hireDate").asText()), subs);

            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
