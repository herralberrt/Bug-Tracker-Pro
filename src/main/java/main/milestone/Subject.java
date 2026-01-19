package main.milestone;

public interface Subject {

    /**
     * Attaches an observer
     */
    void addObs(Observer o);

    /**
     * Notifies all observers
     */
    void notifObs(String message);
}
