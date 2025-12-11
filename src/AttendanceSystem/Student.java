package AttendanceSystem;

import java.util.HashMap;
import java.util.Map;

// Student class with session-wise attendance tracking
public class Student extends Person {
    private String course;
    private Map<String, Boolean> sessionAttendance; // "DAY-TIMESLOT-SUBJECT" -> isPresent
    
    public Student(String name, String id, String course) {
        super(name, id);
        this.course = course;
        this.sessionAttendance = new HashMap<>();
    }
    
    public void markAttendanceForSession(String sessionKey, boolean isPresent) {
        sessionAttendance.put(sessionKey, isPresent);
    }
    
    public boolean getAttendanceForSession(String sessionKey) {
        return sessionAttendance.getOrDefault(sessionKey, false);
    }
    
    public int getTotalSessionsAttended() {
        int count = 0;
        for (boolean present : sessionAttendance.values()) {
            if (present) count++;
        }
        return count;
    }
    
    public int getTotalSessions() {
        return sessionAttendance.size();
    }
    
    public double getAttendancePercentage() {
        if (sessionAttendance.isEmpty()) return 0.0;
        return (getTotalSessionsAttended() * 100.0) / getTotalSessions();
    }
    
    public String getCourse() {
        return course;
    }
    
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("|").append(name).append("|").append(course).append("|");
        sb.append(sessionAttendance.size()).append("|");
        for (Map.Entry<String, Boolean> entry : sessionAttendance.entrySet()) {
            sb.append(entry.getKey()).append("#").append(entry.getValue() ? "1" : "0").append(";");
        }
        return sb.toString();
    }
    
    public static Student fromFileString(String line) {
        String[] parts = line.split("\\|");
        Student student = new Student(parts[1], parts[0], parts[2]);
        if (parts.length > 4 && !parts[4].isEmpty()) {
            String[] sessions = parts[4].split(";");
            for (String session : sessions) {
                if (!session.isEmpty()) {
                    // Split by last occurrence of # to handle keys containing # if any (though unlikely)
                    int lastHash = session.lastIndexOf('#');
                    if (lastHash != -1) {
                        String key = session.substring(0, lastHash);
                        String val = session.substring(lastHash + 1);
                        student.sessionAttendance.put(key, val.equals("1"));
                    }
                }
            }
        }
        return student;
    }
    
    public Map<String, Boolean> getSessionAttendance() {
        return sessionAttendance;
    }
}
