package AttendanceSystem;

// User class for authentication
public class User {
    private String username;
    private String password;
    private String role; // "ADMIN", "TEACHER", or "STUDENT"
    private String[] subjects; // subjects this teacher can access
    private String studentId; // Link to student record if role is STUDENT
    
    public User(String username, String password, String role, String[] subjects) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.subjects = subjects;
        this.studentId = null;
    }
    
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String[] getSubjects() { return subjects; }
    public String getStudentId() { return studentId; }
    
    public void setSubjects(String[] subjects) { this.subjects = subjects; }
    public void setPassword(String password) { this.password = password; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public boolean canAccessSubject(String subject) {
        if (role.equals("ADMIN")) return true;
        // Students can access their own course subjects - handled elsewhere or assume true for now
        if (role.equals("STUDENT")) return true; 
        for (String s : subjects) {
            if (s.equals(subject)) return true;
        }
        return false;
    }
}
