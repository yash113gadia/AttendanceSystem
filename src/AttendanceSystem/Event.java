package AttendanceSystem;

import java.io.Serializable;
import java.util.UUID;

public class Event implements Serializable {
    private String id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String location;
    private String organizer; // Teacher username
    private String status; // "PENDING", "APPROVED", "REJECTED"
    private String affectedSessions; // Comma-separated list of "TIME#SUBJECT" or just "TIME" or keys
    private String passcode; // 4-digit code for proxy prevention

    public Event(String title, String description, String date, String time, String location, String organizer, String affectedSessions) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.organizer = organizer;
        this.status = "PENDING"; // Default status
        this.affectedSessions = affectedSessions;
        this.passcode = String.format("%04d", (int)(Math.random() * 10000)); // Random 4 digits
    }

    // Constructor for loading from file
    public Event(String id, String title, String description, String date, String time, String location, String organizer, String status, String affectedSessions, String passcode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.organizer = organizer;
        this.status = status;
        this.affectedSessions = affectedSessions;
        this.passcode = passcode;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
    public String getOrganizer() { return organizer; }
    public String getStatus() { return status; }
    public String getAffectedSessions() { return affectedSessions; }
    public String getPasscode() { return passcode; }
    
    public void setStatus(String status) { this.status = status; }

    public String toFileString() {
        // Escape pipes
        String safeTitle = title.replace("|", " ");
        String safeDesc = description.replace("|", " ");
        String safeAffected = affectedSessions == null ? "" : affectedSessions;
        return id + "|" + safeTitle + "|" + safeDesc + "|" + date + "|" + time + "|" + location + "|" + organizer + "|" + status + "|" + safeAffected + "|" + passcode;
    }

    public static Event fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 8) {
            String affected = parts.length > 8 ? parts[8] : "";
            String code = parts.length > 9 ? parts[9] : "0000"; // Default legacy
            return new Event(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], affected, code);
        }
        return null;
    }
}
