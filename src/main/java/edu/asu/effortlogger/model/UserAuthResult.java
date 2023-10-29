package edu.asu.effortlogger.model;

import java.util.Optional;

public record UserAuthResult(
        AuthStatus status,
        Optional<User> user,
        int incorrectPasswordAttempts
) {
}
