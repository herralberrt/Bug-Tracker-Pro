package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import main.AppState;

public final class LostInvestors implements Command {

    /**
     * Constructs a LostInvestors command
     */
    public LostInvestors(final ObjectNode node) {
    }

    /**
     * Executes the lost investors command
     */
    @Override
    public void execute() {
        AppState.loseInvestors();
    }

    /**
     * Undoes the lost investors command
     */
    @Override
    public void undo() {
    }
}
