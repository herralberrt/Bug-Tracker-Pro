package main.commands;

public interface Command {

    /**
     * Executes the command
     */
    void execute();

    /**
     * Undoes the command
     */
    void undo();
}
