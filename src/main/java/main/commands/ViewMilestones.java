package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ViewMilestones implements Command {

    private final ObjectNode node;

    public ViewMilestones(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
