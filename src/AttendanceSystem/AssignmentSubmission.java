package AttendanceSystem;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AssignmentSubmission implements Serializable {
    private String assignmentId;
    private String studentId;
    private String studentName;
    private String filePath;
    private String status; // PENDING, APPROVED, REJECTED
    private String feedback;

    public AssignmentSubmission(String assignmentId, String studentId, String studentName, String filePath) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.filePath = filePath;
        this.status = "PENDING";
        this.feedback = "";
    }
    
    // Constructor for loading
    public AssignmentSubmission(String assignmentId, String studentId, String studentName, String filePath, String status, String feedback) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.filePath = filePath;
        this.status = status;
        this.feedback = feedback;
    }

    public String getAssignmentId() { return assignmentId; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getFilePath() { return filePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String toFileString() {
        return assignmentId + "|" + studentId + "|" + studentName + "|" + filePath + "|" + status + "|" + feedback;
    }

    public static AssignmentSubmission fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            return new AssignmentSubmission(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }
        return null;
    }
}
