package main;

import main.milestone.Milestone;
import main.ticket.Ticket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class AppState {

    private static final List<Ticket> tickets = new ArrayList<>();
    private static final List<Milestone> milestones = new ArrayList<>();
    private static int nextTicketId = 0;
    private static boolean investorsLost = false;
    private static LocalDate testingPhaseStart = null;
    private static final int TESTING_PHASE_DURATION = 12;

    private AppState() {

    }

    public static int nextTicketId() {
        return nextTicketId++;
    }

    public static void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public static List<Ticket> getTickets() {
        return tickets;
    }

    public static boolean investorsLost() {
        return investorsLost;
    }

    public static void loseInvestors() {
        investorsLost = true;
    }

    public static void startTestingPhase(String timestamp) {
        if (testingPhaseStart == null) {
            testingPhaseStart = LocalDate.parse(timestamp);
        }
    }

    public static boolean isInTestingPhase(String timestamp) {
        if (testingPhaseStart == null) {
            return false;
        }
        LocalDate currentDate = LocalDate.parse(timestamp);
        LocalDate testingPhaseEnd = testingPhaseStart.plusDays(TESTING_PHASE_DURATION - 1);
        return !currentDate.isAfter(testingPhaseEnd);
    }

    public static boolean isReporter(String username) {
        return App.getUserRole(username).equals("REPORTER");
    }

    public static boolean isManager(String username) {
        return App.getUserRole(username).equals("MANAGER");
    }

    public static boolean isDeveloper(String username) {
        return App.getUserRole(username).equals("DEVELOPER");
    }

    public static List<Ticket> getAllTickets() {
        return tickets;
    }

    public static boolean userExists(String username) {
        return App.userExistsInternal(username);
    }

    public static void addMilestone(Milestone milestone) {
        milestones.add(milestone);
    }

    public static List<Milestone> getMilestones() {
        return milestones;
    }

    public static Milestone getMilestoneByName(String name) {
        for (Milestone m : milestones) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    public static boolean isTicketInMilestone(int ticketId) {
        for (Milestone m : milestones) {
            if (m.getTickets().contains(ticketId)) {
                return true;
            }
        }
        return false;
    }

    public static String getMilestoneNameByTicket(int ticketId) {
        for (Milestone m : milestones) {
            if (m.getTickets().contains(ticketId)) {
                return m.getName();
            }
        }
        return null;
    }

    public static void removeMilestone(Milestone milestone) {
        milestones.remove(milestone);
    }

    public static Ticket getTicketById(int id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }
        return null;
    }

    public static com.fasterxml.jackson.databind.node.ObjectNode getDeveloperByUsername(String username) {
        return App.getDeveloperByUsername(username);
    }

    public static void reset() {
        tickets.clear();
        milestones.clear();
        nextTicketId = 0;
        investorsLost = false;
        testingPhaseStart = null;
    }

    public static List<String> getManagerSubordinates(String managerUsername) {
        com.fasterxml.jackson.databind.node.ObjectNode managerNode = App.getManagerByUsername(managerUsername);
        if (managerNode != null && managerNode.has("subordinates")) {
            List<String> subordinates = new ArrayList<>();
            managerNode.get("subordinates").forEach(node -> subordinates.add(node.asText()));
            return subordinates;
        }
        return new ArrayList<>();
    }

    public static void checkMilestoneNotifications(String timestamp) {
        LocalDate currentDate = LocalDate.parse(timestamp);

        for (Milestone milestone : milestones) {
            if (milestone.isBlocked()) {
                continue;
            }

            LocalDate dayBeforeDue = milestone.getDueDate().minusDays(1);
            if (currentDate.equals(dayBeforeDue)) {
                String message = "Milestone " + milestone.getName() +
                        " is due tomorrow. All unresolved tickets are now CRITICAL.";
                for (String dev : milestone.getAssignedDevs()) {
                    App.addNotification(dev, timestamp, message);
                }
            }
        }
    }

    public static void checkMilestoneUnblocking(String blockingMilestoneName, int closedTicketId, String timestamp) {
        LocalDate currentDate = LocalDate.parse(timestamp);
        Milestone blockingMilestone = getMilestoneByName(blockingMilestoneName);

        if (blockingMilestone == null) {
            return;
        }

        if (!blockingMilestone.containsAnyOpenTickets()) {
            for (Milestone milestone : milestones) {
                if (blockingMilestone.getName().equals(milestone.getName())) {
                    continue;
                }

                boolean wasBlocked = false;
                for (Milestone m : milestones) {
                    if (m.getName().equals(blockingMilestoneName)) {
                        if (m.getBlockingFor().contains(milestone.getName())) {
                            wasBlocked = true;
                            break;
                        }
                    }
                }

                if (wasBlocked) {
                    if (currentDate.isAfter(milestone.getDueDate())) {
                        String message = "Milestone " + milestone.getName() +
                                " was unblocked after due date. All active tickets are now CRITICAL.";
                        for (String dev : milestone.getAssignedDevs()) {
                            App.addNotification(dev, timestamp, message);
                        }
                    } else {
                        String message = "Milestone " + milestone.getName() +
                                " is now unblocked as ticket " + closedTicketId + " has been CLOSED.";
                        for (String dev : milestone.getAssignedDevs()) {
                            App.addNotification(dev, timestamp, message);
                        }
                    }
                }
            }
        }
    }
}

