package oikos.app.analytics;

import lombok.Getter;

import java.util.Optional;

@Getter
public class ViewRecord {
    private final Integer year;
    private final Integer month;
    private Integer week;
    private Integer day;
    private final Integer views;


    public ViewRecord(int year, int month, Optional<Integer> weekOrDay, int views, DetailLevel detailLevel) {
        this.year = year;
        this.month = month;
        if (detailLevel == DetailLevel.WEEK) {
            this.week = weekOrDay.get();
        } else if (detailLevel == DetailLevel.DAY) {
            this.day = weekOrDay.get();
        }
        this.views = views;
    }
}
