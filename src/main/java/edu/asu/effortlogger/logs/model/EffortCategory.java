package edu.asu.effortlogger.logs.model;

import java.util.ArrayList;
import java.util.List;

public record EffortCategory(String name, List<Subordinate> subs) {

    public EffortCategory(String name) {
        this(name, new ArrayList<>());
    }
}
