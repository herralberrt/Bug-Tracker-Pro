package main;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.enums.BusinessPriority;
import main.enums.ExpertiseArea;
import main.enums.Seniority;
import main.milestone.Milestone;
import main.ticket.Ticket;
import main.utiliz.Developer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public final class AppState {
    private static final Map<String, Developer> DEVELOPER_INSTANCES = new HashMap<>();

    /**
     * Returns the Developer instance for the given username
     */
    public static Developer getDeveloperInstanceByUsername(final String username) {
        if (DEVELOPER_INSTANCES.containsKey(username)) {
            return DEVELOPER_INSTANCES.get(username);
        }
        ObjectNode node = getDeveloperByUsername(username);
        if (node == null) {
            return null;
        }
        LocalDate hireDate = LocalDate.parse(node.get("hireDate").asText());
        Seniority seniority = Seniority.valueOf(node.get("seniority").asText());
        ExpertiseArea expertiseArea = ExpertiseArea.valueOf(node.get("expertiseArea").asText());
        Developer dev = new Developer(username,
                node.get("email").asText(), hireDate, seniority, expertiseArea);
        DEVELOPER_INSTANCES.put(username, dev);
        return dev;
    }


    private static final List<Ticket> TICKETS = new ArrayList<>();
    private static final List<Milestone> MILESTONES = new ArrayList<>();
    private static int nextTicketId = 0;
    private static boolean investorsLost = false;
    private static LocalDate testingPhaseStart = null;
    private static final int TESTING_PHASE_DURATION = 12;

    /**
     * Private constructor to prevent instantiation
     */
    private AppState() {

    }

    /**
     * Returns the next ticket id and increments the counter
     */
    public static int nextTicketId() {
        return nextTicketId++;
    }

    /**
     * Adds a ticket to the ticket list
     */
    public static void addTicket(final Ticket ticket) {
        TICKETS.add(ticket);
    }

    /**
     * Returns the list of tickets
     */
    public static List<Ticket> getTickets() {
        return TICKETS;
    }

    /**
     * Sets the investorsLost flag to true
     */
    public static void loseInvestors() {
        investorsLost = true;
    }

    /**
     * Starts the testing phase at the given timestamp
     */
    public static void startTestingPhase(final String timestamp) {
        testingPhaseStart = LocalDate.parse(timestamp);
    }

    /**
     * Returns true if the given timestamp is within the testing phase
     */
    public static boolean inPhase(final String timestamp) {
        if (testingPhaseStart == null) {
            return false;
        }
        LocalDate currDate = LocalDate.parse(timestamp);
        LocalDate testEnd =
                testingPhaseStart.plusDays(TESTING_PHASE_DURATION - 1);
        return !currDate.isAfter(testEnd);
    }

    /**
     * Returns true if the user is a reporter
     */
    public static boolean isReporter(final String username) {
        return App.getUserRole(username).equals("REPORTER");
    }

    /**
     * Returns true if the user is a manager
     */
    public static boolean isManager(final String username) {
        return App.getUserRole(username).equals("MANAGER");
    }

    /**
     * Returns true if the user is a developer
     */
    public static boolean isDeveloper(final String username) {
        return App.getUserRole(username).equals("DEVELOPER");
    }

    /**
     * Returns all tickets
     */
    public static List<Ticket> getAllTickets() {
        return TICKETS;
    }

    /**
     * Returns true if the user exists
     */
    public static boolean userExists(final String username) {
        return App.userExistsInternal(username);
    }

    /**
     * Adds a milestone to the milestone list
     */
    public static void addMilestone(final Milestone milestone) {
        MILESTONES.add(milestone);
    }

    /**
     * Returns the list of milestones
     */
    public static List<Milestone> getMilestones() {
        return MILESTONES;
    }

    /**
     * Returns the milestone with the given name
     */
    public static Milestone getMilestoneByName(final String name) {
        for (Milestone milestone : MILESTONES) {
            if (milestone.getName().equals(name)) {
                return milestone;
            }
        }
        return null;
    }

    /**
     * Returns true if the ticket is in any milestone
     */
    public static boolean isTicketInMilestone(final int ticketId) {
        for (Milestone m : MILESTONES) {
            if (m.getTickets().contains(ticketId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the milestone name for the given ticket id
     */
    public static String getMilestoneNameByTicket(final int ticketId) {
        for (Milestone m : MILESTONES) {
            if (m.getTickets().contains(ticketId)) {
                return m.getName();
            }
        }
        return null;
    }

    /**
     * Removes the given milestone from the milestone list
     */
    public static void removeMilestone(final Milestone milestone) {
        MILESTONES.remove(milestone);
    }

    /**
     * Returns the ticket with the given id
     */
    public static Ticket getTicketById(final int id) {
        for (Ticket ticket : TICKETS) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * Returns the developer node for the given username
     */
    public static ObjectNode getDeveloperByUsername(final String username) {
        return App.getDeveloperByUsername(username);
    }

    /**
     * Resets the application state
     */
    public static void reset() {
        TICKETS.clear();
        MILESTONES.clear();
        nextTicketId = 0;
        investorsLost = false;
        testingPhaseStart = null;
        DEVELOPER_INSTANCES.clear();
    }

    /**
     * Returns the list of subordinates for the given manager
     */
    public static List<String> getManSubord(final String managerUsername) {
        ObjectNode managerNode = App.getManagerByUsername(managerUsername);
        if (managerNode != null && managerNode.has("subordinates")) {

            List<String> subordinates = new ArrayList<>();

            managerNode.get("subordinates").forEach(node -> subordinates.add(node.asText()));
            return subordinates;
        }
        return new ArrayList<>();
    }

    /**
     * Checks and sends notifications for milestones due tomorrow
     */
    public static void checkMilestone(final String timestamp) {
        LocalDate currDate = LocalDate.parse(timestamp);

        for (Milestone milestone : MILESTONES) {
            if (milestone.isBlocked()) {
                continue;
            }
            LocalDate dayBefore = milestone.getDueDate().minusDays(1);
            if (currDate.equals(dayBefore)) {
                String message = "Milestone " + milestone.getName()
                        + " is due tomorrow. All unresolved tickets are now CRITICAL.";
                milestone.notifObs(message);
            }
        }
    }

    /**
     * Checks and sends notifications when a milestone is unblocked
     */
    public static void milesUnblock(final String blockingMilestoneName,
            final int closedTicketId, final String timestamp) {

        LocalDate currDate = LocalDate.parse(timestamp);
        Milestone blockingMilestone = getMilestoneByName(blockingMilestoneName);

        if (blockingMilestone == null) {
            return;
        }

        if (!blockingMilestone.containsAnyOpenTickets()) {
            for (Milestone milestone : MILESTONES) {
                if (blockingMilestone.getName().equals(milestone.getName())) {
                    continue;
                }
                boolean blocked = false;
                for (Milestone m : MILESTONES) {
                    if (m.getName().equals(blockingMilestoneName)) {
                        if (m.getBlockingFor().contains(milestone.getName())) {
                            blocked = true;
                            break;
                        }
                    }
                }
                if (blocked) {
                    String msg;
                    if (currDate.isAfter(milestone.getDueDate())) {
                        msg = "Milestone " + milestone.getName()
                                + " was unblocked after due date. All active tickets are now CRITICAL.";
                    } else {
                        msg = "Milestone " + milestone.getName()
                                + " is now unblocked as ticket " + closedTicketId + " has been CLOSED.";
                    }
                    milestone.notifObs(msg);
                }
            }
        }
    }

    /**
     * Returns true if there are any active milestones
     */
    public static boolean actMiles() {
        for (Milestone milestone : MILESTONES) {
            if (milestone.containsAnyOpenTickets()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the escalated priority for a ticket at a given timestamp
     */
    public static BusinessPriority calcPriorityEsc(final Ticket ticket,
                                                   final String timestamp) {

        Milestone milestone = null;
        
        for (Milestone mile : MILESTONES) {
            if (mile.getTickets().contains(ticket.getId())) {
                milestone = mile;
                break;
            }
        }

        if (milestone == null) {
            return ticket.getBusinessPriority();
        }

        if (milestone.isBlocked()) {
            return ticket.getBusinessPriority();
        }

        LocalDate currDate = LocalDate.parse(timestamp);
        LocalDate crMiles = milestone.getCreatedAt();
        LocalDate dueDate = milestone.getDueDate();

        LocalDate dayBefore = dueDate.minusDays(1);
        if (!currDate.isBefore(dayBefore)) {
            return BusinessPriority.CRITICAL;
        }

        long crDays = ChronoUnit.DAYS.between(crMiles, currDate);
        final int ESCAL = 3;
        int escLevels = (int) (crDays / ESCAL);

        BusinessPriority prOriginal = ticket.getBusinessPriority();
        BusinessPriority prEscalation = prOriginal;

        for (int i = 0; i < escLevels; i++) {
            prEscalation = switch (prEscalation) {

                case LOW ->
                        BusinessPriority.MEDIUM;

                case MEDIUM ->
                        BusinessPriority.HIGH;

                case HIGH ->
                        BusinessPriority.CRITICAL;

                case CRITICAL ->
                        BusinessPriority.CRITICAL;
            };
        }
        return prEscalation;
    }
}

