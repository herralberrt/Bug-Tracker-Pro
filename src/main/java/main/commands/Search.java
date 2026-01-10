package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Search implements Command {

    private final ObjectNode node;

    public Search(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
