package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AddComment implements Command {

    private final ObjectNode node;

    public AddComment(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
