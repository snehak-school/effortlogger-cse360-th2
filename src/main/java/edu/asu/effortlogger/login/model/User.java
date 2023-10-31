package edu.asu.effortlogger.login.model;

public record User(
        int id,
        String token,
        String name
) {
}
