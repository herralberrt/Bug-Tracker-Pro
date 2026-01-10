package main.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class GeneratePerformanceReport implements Command {

    private final ObjectNode node;

    public GeneratePerformanceReport(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }
}
