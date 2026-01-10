package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewAssignedTickets implements Command {

    private final ObjectNode node;

    public ViewAssignedTickets(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
