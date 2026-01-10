package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AssignTicket implements Command {

    private final ObjectNode node;

    public AssignTicket(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
