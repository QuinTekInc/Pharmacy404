package data.models;

import java.time.LocalDate;

public class Log {
    LocalDate date;

    String title;
    String details;

    int id;

    public Log(int id, LocalDate date, String title, String details){
        this.id = id;
        this.date = date;
        this.title = title;
        this.details = details;
    }

    public int getId() {
        return id;
    }
    public LocalDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }
}
