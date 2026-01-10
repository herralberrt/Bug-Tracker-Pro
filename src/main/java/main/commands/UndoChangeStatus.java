package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import main.App;

public class UndoChangeStatus implements Command {

    private final ObjectNode node;

    public UndoChangeStatus(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
