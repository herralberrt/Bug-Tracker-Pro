package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewTicketHistory implements Command {

    private final ObjectNode node;

    public ViewTicketHistory(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
