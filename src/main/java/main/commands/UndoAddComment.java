package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class UndoAddComment implements Command {

    private final ObjectNode node;

    public UndoAddComment(final ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
