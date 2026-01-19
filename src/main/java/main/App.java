package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import main.commands.Command;
import main.commands.CommandFactory;
import java.io.IOException;


public final class App {

    private App() {

    }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";
    private static final ObjectWriter WRITER =
            new ObjectMapper().writer().withDefaultPrettyPrinter();
    private static final List<ObjectNode> users = new ArrayList<>();
    private static final List<ObjectNode> OUTPUTS = new ArrayList<>();

    /**
     * The main entry point for the application
     */
    public static void main(final String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: App <inputPath> <outputPath>");
            return;
        }
        run(args[0], args[1]);
    }

    /**
     * Runs the application with the given input and output paths
     */
    public static void run(final String inputPath, final String outputPath) {
        OUTPUTS.clear();
        users.clear();
        AppState.reset();

        ObjectMapper mapper = new ObjectMapper();

        try {
            users.addAll(mapper.readValue(new File(INPUT_USERS_FIELD),
                            mapper.getTypeFactory()
                                    .constructCollectionType(List.class, ObjectNode.class)));
        } catch (IOException e) {
            System.out.println("Error reading users file");
        }

        List<ObjectNode> commands;
        try {
            commands = mapper.readValue(
                    new File(inputPath),
                    mapper.getTypeFactory()
                            .constructCollectionType(List.class, ObjectNode.class));
        } catch (IOException e) {
            System.out.println("Error reading commands file");
            return;
        }

        if (!commands.isEmpty()) {
            AppState.startTestingPhase(commands.get(0).get("timestamp").asText());
        }

        for (ObjectNode commandNode : commands) {
            String username = commandNode.get("username").asText();
            String timestamp = commandNode.get("timestamp").asText();
            AppState.checkMilestone(timestamp);

            if (!userExists(username)) {
                OUTPUTS.add(buildUserNotFoundError(commandNode));
                continue;
            }

            try {
                Command command = CommandFactory.INSTANCE.createCommand(commandNode);
                command.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            File outputFile = new File(outputPath);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            WRITER.writeValue(outputFile, OUTPUTS);
        } catch (IOException e) {
            System.out.println("Error writing output file");
        }
    }

    /**
     * Adds an output node to the output list
     */
    public static void addOutput(final ObjectNode out) {
        OUTPUTS.add(out);
    }

    /**
     * Returns the role of the user with the given username
     */
    public static String getUserRole(final String username) {
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

    /**
     * Checks if a user exists with the given username
     */
    private static boolean userExists(final String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks internally if a user exists with the given username
     */
    public static boolean userExistsInternal(final String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the developer user node for the given username
     */
    public static ObjectNode getDeveloperByUsername(final String username) {
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

    /**
     * Returns the manager user node for the given username
     */
    public static ObjectNode getManagerByUsername(final String username) {
        for (ObjectNode user : users) {
            if (user.get("username").asText().equals(username)) {
                if (user.has("role") && user.get("role").asText().equals("MANAGER")) {
                    return user;
                }
                break;
            }
        }
        return null;
    }

    /**
     * Builds an error node for a user not found
     */
    private static ObjectNode buildUserNotFoundError(final ObjectNode commandNode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode error = mapper.createObjectNode();

        error.put("command", commandNode.get("command").asText());
        error.put("username", commandNode.get("username").asText());
        error.put("timestamp", commandNode.get("timestamp").asText());
        error.put("error", "The user "
                + commandNode.get("username").asText() + " does not exist.");
        return error;
    }

    /**
     * Adds a notification for the given user
     */
    public static void addNotification(final String username,
                                       final String timestamp,
                                       final String message) {
        main.utiliz.Developer dev = main.AppState.getDeveloperInstanceByUsername(username);
        if (dev != null) {
            dev.update(message);
        }
    }
}
