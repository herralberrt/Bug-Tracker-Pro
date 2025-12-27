package main.milestone;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Milestone implements Subject {

    private final String name;
    private final List<String> blockingFor;
    private final LocalDate dueDate;
    private final List<Integer> tickets;
    private final List<String> assignedDevs;

    private final List<Observer> observers = new ArrayList<>();

    public Milestone(String name, List<String> blockingFor, LocalDate dueDate,
                     List<Integer> tickets, List<String> assignedDevs) {

        this.name = name;
        this.blockingFor = blockingFor;
        this.dueDate = dueDate;
        this.tickets = tickets;
        this.assignedDevs = assignedDevs;
    }

    @Override
    public void attach(Observer o) {
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer o : observers) {
            o.update(message);
        }
    }
}
