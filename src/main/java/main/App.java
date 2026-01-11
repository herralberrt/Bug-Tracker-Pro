package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.commands.Command;
import main.commands.CommandFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class App {

    private App() { }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";

    private static final ObjectWriter WRITER =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    private static final List<ObjectNode> users = new ArrayList<>();
    private static final List<ObjectNode> outputs = new ArrayList<>();
    private static final Map<String, List<String>> notifications = new HashMap<>();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: App <inputPath> <outputPath>");
            return;
        }
        run(args[0], args[1]);
    }

    public static void run(final String inputPath, final String outputPath) {
        outputs.clear();
        users.clear();

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

        if (!commands.isEmpty()) {
            AppState.startTestingPhase(commands.get(0).get("timestamp").asText());
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
                
                if (AppState.investorsLost()) {
                    break;
                }
            } catch (Exception ignored) {
            }
        }

        try {
            File outputFile = new File(outputPath);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            WRITER.writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("Error writing output file");
        }
    }

    public static void addOutput(ObjectNode out) {
        outputs.add(out);
    }

    public static String getUserRole(String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                if (user.has("role")) {
                    return user.get("role").asText();
                }
                break;
            }
        }
        return "";
    }

    private static boolean userExists(String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static boolean userExistsInternal(String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static boolean userExistsPublic(String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static ObjectNode getDeveloperByUsername(String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                if (user.has("role") && user.get("role").asText().equals("DEVELOPER")) {
                    return user;
                }
                break;
            }
        }
        return null;
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

    public static void addNotification(String username, String timestamp, String message) {
        notifications.computeIfAbsent(username, k -> new ArrayList<>()).add(message);
    }
}
