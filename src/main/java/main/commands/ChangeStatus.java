package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;

public class ChangeStatus implements Command {

    private final ObjectNode node;

    public ChangeStatus(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
