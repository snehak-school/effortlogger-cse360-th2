package edu.asu.effortlogger.logs.model;

import java.time.LocalDateTime;

public record EffortLog(LocalDateTime startTime, LocalDateTime endTime, int project, int lcStep, int lc, int subi, String subs, int id) {
    @Override
    public String toString()
    {
        return startTime.toString().substring(0, 19) + " - " +
               endTime.toString().substring(0, 19);
    }
}
