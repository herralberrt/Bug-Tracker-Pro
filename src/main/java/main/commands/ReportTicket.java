package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;

public class ReportTicket implements Command {

    private final ObjectNode node;

    public ReportTicket(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
