package AttendanceSystem;

import java.io.Serializable;
import java.util.UUID;

public class Assignment implements Serializable {
    private String id;
    private String subject;
    private String title;
    private String description;
    private String deadline;
    private String createdBy; // Teacher username

    public Assignment(String subject, String title, String description, String deadline, String createdBy) {
        this.id = UUID.randomUUID().toString();
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.createdBy = createdBy;
    }
    
    // Constructor for loading from file
    public Assignment(String id, String subject, String title, String description, String deadline, String createdBy) {
        this.id = id;
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.createdBy = createdBy;
    }

    public String getId() { return id; }
    public String getSubject() { return subject; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }
    public String getCreatedBy() { return createdBy; }

    public String toFileString() {
        // Escape pipes in content just in case
        String cleanDesc = description.replace("|", " ");
        String cleanTitle = title.replace("|", " ");
        return id + "|" + subject + "|" + cleanTitle + "|" + cleanDesc + "|" + deadline + "|" + createdBy;
    }

    public static Assignment fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            return new Assignment(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }
        return null;
    }
}
