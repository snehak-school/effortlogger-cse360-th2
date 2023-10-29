package edu.asu.effortlogger.model;

public record User(
        int id,
        String token,
        String name
) {
}
