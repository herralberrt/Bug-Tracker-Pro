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
        App.changeStatus(node);
    }

    @Override
    public void undo() {
        App.undoChangeStatus(node);
    }
}
