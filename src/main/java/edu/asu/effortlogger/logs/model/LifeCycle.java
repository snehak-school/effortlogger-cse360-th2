package edu.asu.effortlogger.logs.model;

public record LifeCycle(String name, int ec, int d) {

    @Override
    public String toString() {
        return name;
    }
}
