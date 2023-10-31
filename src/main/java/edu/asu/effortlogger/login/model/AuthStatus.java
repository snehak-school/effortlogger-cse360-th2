package edu.asu.effortlogger.login.model;

public enum AuthStatus {
    SUCCESS,
    UNKNOWN_ERROR,
    USER_NOT_FOUND,
    INCORRECT_PASSWORD,
    TOO_MANY_FAILED_LOGINS
}
