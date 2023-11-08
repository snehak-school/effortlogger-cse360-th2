package edu.asu.effortlogger.logs.model;

public record Defect(
        int id,
        int project,
        String name,
        boolean open,
        String info,
        int lcInject,
        int lcRemove,
        int category,
        int fixDefect
) {

    @Override
    public String toString() {
        return name;
    }
}
