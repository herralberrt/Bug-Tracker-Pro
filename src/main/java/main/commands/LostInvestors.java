package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import main.AppState;

public class LostInvestors implements Command {

    public LostInvestors(ObjectNode node) {}

    @Override
    public void execute() {
        AppState.loseInvestors();
    }

    @Override
    public void undo() {}
}
