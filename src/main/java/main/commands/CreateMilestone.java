package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreateMilestone implements Command {

    private final ObjectNode node;

    public CreateMilestone(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
