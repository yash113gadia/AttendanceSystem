package AttendanceSystem;

// Class Schedule representation with timetable structure
public class ClassSession {
    private String day;
    private String timeSlot;
    private String subject;
    private String teacher;
    private String room;
    private String course;
    
    public ClassSession(String day, String timeSlot, String subject, String teacher, String room, String course) {
        this.day = day;
        this.timeSlot = timeSlot;
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
        this.course = course;
    }
    
    // Legacy/Convenience constructor for backward compat if strictly needed, 
    // but better to update calls. I'll stick to updating calls.
    
    public String getDay() { return day; }
    public String getTimeSlot() { return timeSlot; }
    public String getSubject() { return subject; }
    public String getTeacher() { return teacher; }
    public String getRoom() { return room; }
    public String getCourse() { return course; }
    
    // Legacy getter
    public String getFacultyRoom() { return room; } 

    @Override
    public String toString() {
        return day + " - " + timeSlot + ": " + subject + " (" + teacher + ", " + room + ") [" + course + "]";
    }
}
