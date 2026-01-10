package main;

import main.ticket.Ticket;
import java.util.ArrayList;
import java.util.List;

public final class AppState {

    private static final List<Ticket> tickets = new ArrayList<>();
    private static int nextTicketId = 0;
    private static boolean investorsLost = false;

    private AppState() {}

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

    public static boolean isReporter(String username) {
        return App.getUserRole(username).equals("REPORTER");
    }

    public static boolean isManager(String username) {
        return App.getUserRole(username).equals("MANAGER");
    }

    public static boolean userExists(String username) {
        return App.userExistsInternal(username);
    }
}
