package AttendanceSystem;

import java.io.Serializable;

public class EventPhoto implements Serializable {
    private String eventId;
    private String studentId;
    private String studentName;
    private String filePath;
    private String status; // PENDING, APPROVED, REJECTED

    public EventPhoto(String eventId, String studentId, String studentName, String filePath) {
        this.eventId = eventId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.filePath = filePath;
        this.status = "PENDING";
    }
    
    // Constructor for loading
    public EventPhoto(String eventId, String studentId, String studentName, String filePath, String status) {
        this.eventId = eventId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.filePath = filePath;
        this.status = status;
    }

    public String getEventId() { return eventId; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getFilePath() { return filePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String toFileString() {
        return eventId + "|" + studentId + "|" + studentName + "|" + filePath + "|" + status;
    }

    public static EventPhoto fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            return new EventPhoto(parts[0], parts[1], parts[2], parts[3], parts[4]);
        }
        return null;
    }
}
