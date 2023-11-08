package edu.asu.effortlogger.logs.model;

public record Subordinate(String name) {

    @Override
    public String toString() {
        return name;
    }
}
