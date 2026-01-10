package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.commands.Command;
import main.commands.CommandFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {

    private App() { }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";

    private static final ObjectWriter WRITER =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    private static final List<ObjectNode> users = new ArrayList<>();

    public static void run(final String inputPath, final String outputPath) {
        List<ObjectNode> outputs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            users.addAll(
                    mapper.readValue(
                            new File(INPUT_USERS_FIELD),
                            mapper.getTypeFactory()
                                    .constructCollectionType(List.class, ObjectNode.class)
                    )
            );
        } catch (IOException e) {
            System.out.println("Error reading users file");
        }

        List<ObjectNode> commands;
        try {
            commands = mapper.readValue(
                    new File(inputPath),
                    mapper.getTypeFactory()
                            .constructCollectionType(List.class, ObjectNode.class)
            );
        } catch (IOException e) {
            System.out.println("Error reading commands file");
            return;
        }

        for (ObjectNode commandNode : commands) {

            String username = commandNode.get("username").asText();

            if (!userExists(username)) {
                outputs.add(buildUserNotFoundError(commandNode));
                continue;
            }

            try {
                Command command = CommandFactory.createCommand(commandNode);
                command.execute();
            } catch (Exception ignored) {
            }
        }

        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("Error writing output file");
        }
    }

    private static boolean userExists(String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private static ObjectNode buildUserNotFoundError(ObjectNode commandNode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode error = mapper.createObjectNode();

        error.put("command", commandNode.get("command").asText());
        error.put("username", commandNode.get("username").asText());
        error.put("timestamp", commandNode.get("timestamp").asText());
        error.put("error", "The user " + commandNode.get("username").asText() + " does not exist.");

        return error;
    }

    public static void changeStatus(ObjectNode node) {
    }

    public static void undoChangeStatus(ObjectNode node) {
    }
}
