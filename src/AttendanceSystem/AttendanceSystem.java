package AttendanceSystem;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

// Attendance System class with timetable
public class AttendanceSystem {
    private Student[] students;
    private int studentCount;
    private static final int MAX_STUDENTS = 500;
    private static final String DATA_FILE = "attendance_data.txt";
    private static final String TIMETABLE_FILE = "timetables_data.txt";
    private static final String REPORT_FILE = "attendance_report.txt";
    private static final String ASSIGNMENTS_FILE = "assignments_data.txt";
    private static final String SUBMISSIONS_FILE = "submissions_data.txt";
    private static final String EVENTS_FILE = "events_data.txt";
    private static final String EVENT_PHOTOS_FILE = "event_photos.txt";
    private static final LocalDate TERM_START_DATE = LocalDate.of(2025, 12, 1);
    
    // Map course name -> List of sessions
    private Map<String, ArrayList<ClassSession>> courseTimetables;
    private Set<String> knownSessionKeys;
    
    private List<Assignment> assignments;
    private List<AssignmentSubmission> submissions;
    private List<Event> events;
    private List<EventPhoto> eventPhotos;
    
    public AttendanceSystem() {
        students = new Student[MAX_STUDENTS];
        studentCount = 0;
        courseTimetables = new HashMap<>();
        knownSessionKeys = new HashSet<>();
        assignments = new ArrayList<>();
        submissions = new ArrayList<>();
        events = new ArrayList<>();
        eventPhotos = new ArrayList<>();
        
        initializeTimetable(); // Load defaults first
        loadTimetables();      // Override/Extend with saved data
        loadDataFromFile();    // Load students
        loadAssignments();     // Load assignments
        loadSubmissions();     // Load submissions
        loadEvents();          // Load events
        loadEventPhotos();     // Load event photos
        
        // After loading students, populate past sessions for all known courses
        populatePastSessions();
    }
    
    public String[] getAllCourses() {
        Set<String> courses = new HashSet<>();
        // Add courses from students
        for (int i = 0; i < studentCount; i++) {
            courses.add(students[i].getCourse());
        }
        // Add courses from timetables
        courses.addAll(courseTimetables.keySet());
        
        String[] sortedCourses = courses.toArray(new String[0]);
        Arrays.sort(sortedCourses);
        return sortedCourses;
    }

    private String getDayString(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "MON";
            case TUESDAY: return "TUE";
            case WEDNESDAY: return "WED";
            case THURSDAY: return "THU";
            case FRIDAY: return "FRI";
            case SATURDAY: return "SAT";
            case SUNDAY: return "SUN";
            default: return "";
        }
    }
    
    // ... existing getters ...

    // Helper to get all sessions (backward compatibility / view all)
    public ArrayList<ClassSession> getTimetable() {
        ArrayList<ClassSession> allSessions = new ArrayList<>();
        for (ArrayList<ClassSession> list : courseTimetables.values()) {
            allSessions.addAll(list);
        }
        return allSessions;
    }

    public void addSessionToCourse(String course, ClassSession session) {
        courseTimetables.computeIfAbsent(course, k -> new ArrayList<>()).add(session);
        
        // Auto-create teacher credentials if teacher doesn't exist
        String teacherName = session.getTeacher();
        if (teacherName != null && !teacherName.trim().isEmpty()) {
            boolean isNew = AuthenticationManager.addTeacherIfNotExists(teacherName, session.getSubject());
            if (isNew) {
                System.out.println("Auto-created teacher account: " + teacherName.toUpperCase() + " with password: " + teacherName.toLowerCase() + "123");
            }
        }
        
        saveTimetables();
    }
    
    public void clearCourseTimetable(String course) {
        courseTimetables.remove(course);
        saveTimetables();
    }

    private void saveTimetables() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TIMETABLE_FILE))) {
            for (Map.Entry<String, ArrayList<ClassSession>> entry : courseTimetables.entrySet()) {
                String course = entry.getKey();
                for (ClassSession s : entry.getValue()) {
                    // Format: COURSE|DAY|TIME|SUBJECT|TEACHER|ROOM
                    writer.println(course + "|" + s.getDay() + "|" + s.getTimeSlot() + "|" + s.getSubject() + "|" + s.getTeacher() + "|" + s.getRoom());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving timetables: " + e.getMessage());
        }
    }
    
    private void loadTimetables() {
        File file = new File(TIMETABLE_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(TIMETABLE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    String course = parts[0];
                    ClassSession session = new ClassSession(parts[1], parts[2], parts[3], parts[4], parts[5], course);
                    courseTimetables.computeIfAbsent(course, k -> new ArrayList<>()).add(session);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading timetables: " + e.getMessage());
        }
    }

    public String[] getUniqueDays() {
        LinkedHashSet<String> days = new LinkedHashSet<>();
        for (ArrayList<ClassSession> list : courseTimetables.values()) {
            for (ClassSession session : list) {
                days.add(session.getDay());
            }
        }
        // Ensure standard order
        String[] standardDays = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        ArrayList<String> sortedDays = new ArrayList<>();
        for (String d : standardDays) {
            if (days.contains(d)) sortedDays.add(d);
        }
        return sortedDays.toArray(new String[0]);
    }

    public ArrayList<ClassSession> getSessionsByDay(String day) {
         // Return all sessions for the day across all courses (merged view)
         ArrayList<ClassSession> sessions = new ArrayList<>();
         for (ArrayList<ClassSession> list : courseTimetables.values()) {
             for (ClassSession s : list) {
                 if (s.getDay().equals(day)) {
                     sessions.add(s);
                 }
             }
         }
         return sessions;
    }
    
    public ArrayList<ClassSession> getSessionsByDay(String day, String course) {
        ArrayList<ClassSession> sessions = new ArrayList<>();
        if (course.equals("All Courses")) {
            return getSessionsByDay(day);
        }
        
        List<ClassSession> courseList = courseTimetables.get(course);
        if (courseList != null) {
            for (ClassSession s : courseList) {
                if (s.getDay().equals(day)) {
                    sessions.add(s);
                }
            }
        }
        return sessions;
    }



    // ... existing methods ...

    private void initializeTimetable() {
        // Default timetable removed as requested.
        // Users must upload timetables via CSV or add them programmatically.
    }
    
    // ... rest of class ...

    
    public void addStudent(String name, String id, String course) {
        if (studentCount < MAX_STUDENTS) {
            Student newStudent = new Student(name, id, course);
            // Sync only this student's course sessions
            Set<String> keys = getPastSessionKeysForCourse(course);
            for (String key : keys) {
                newStudent.markAttendanceForSession(key, false);
            }
            students[studentCount++] = newStudent;
            saveDataToFile();
        }
    }
    
    public Student findStudent(String id) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getId().equals(id)) {
                return students[i];
            }
        }
        return null;
    }
    
    public void markAttendanceForSession(String studentId, String sessionKey, boolean isPresent) {
        // Updated: No longer using global knownSessionKeys blindly.
        // We assume sessionKey is valid or we trust the caller (UI which generates it from timetable)
        
        Student student = findStudent(studentId);
        if (student != null) {
            student.markAttendanceForSession(sessionKey, isPresent);
            saveDataToFile();
        }
    }
    
    public boolean removeStudent(String id) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getId().equals(id)) {
                for (int j = i; j < studentCount - 1; j++) {
                    students[j] = students[j + 1];
                }
                students[studentCount - 1] = null;
                studentCount--;
                saveDataToFile();
                return true;
            }
        }
        return false;
    }
    
    public Student[] getAllStudents() {
        Student[] result = new Student[studentCount];
        for (int i = 0; i < studentCount; i++) {
            result[i] = students[i];
        }
        return result;
    }
    
    public int getStudentCount() {
        return studentCount;
    }
    
    public void generateReport() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(REPORT_FILE))) {
            writer.println("========================================");
            writer.println("    ATTENDANCE REPORT");
            writer.println("    Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            writer.println("========================================\n");
            
            if (studentCount == 0) {
                writer.println("No students registered.");
                return;
            }
            
            int totalStudents = studentCount;
            double avgAttendance = 0.0;
            int studentsBelow75 = 0;
            
            for (int i = 0; i < studentCount; i++) {
                avgAttendance += students[i].getAttendancePercentage();
                if (students[i].getAttendancePercentage() < 75.0) {
                    studentsBelow75++;
                }
            }
            avgAttendance /= totalStudents;
            
            writer.println("SUMMARY:");
            writer.println("Total Students: " + totalStudents);
            writer.printf("Average Attendance: %.2f%%\n", avgAttendance);
            writer.println("Students Below 75%%: " + studentsBelow75);
            writer.println("\n========================================\n");
            writer.println("INDIVIDUAL ATTENDANCE:");
            writer.println("----------------------------------");
            
            for (int i = 0; i < studentCount; i++) {
                Student s = students[i];
                writer.println("Student ID: " + s.getId());
                writer.println("Name: " + s.getName());
                writer.println("Course: " + s.getCourse());
                writer.println("Total Sessions: " + s.getTotalSessions());
                writer.println("Sessions Attended: " + s.getTotalSessionsAttended());
                writer.printf("Attendance: %.2f%%", s.getAttendancePercentage());
                
                if (s.getAttendancePercentage() < 75.0) {
                    writer.print(" [LOW ATTENDANCE - WARNING]");
                }
                writer.println("\n----------------------------------");
            }
            
            writer.println("\nReport Generated Successfully!");
            
        } catch (IOException e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }
    
    private void saveDataToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (int i = 0; i < studentCount; i++) {
                writer.println(students[i].toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
    
    private void loadDataFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            studentCount = 0;
            while ((line = reader.readLine()) != null && studentCount < MAX_STUDENTS) {
                students[studentCount++] = Student.fromFileString(line);
            }
            
            // Populate known keys and sync
            // Replaced by syncGlobalSessions logic which handles course specifics
            syncGlobalSessions();
            
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
    
    private void syncGlobalSessions() {
        boolean changed = false;
        for (int i = 0; i < studentCount; i++) {
            Student s = students[i];
            Set<String> keys = getPastSessionKeysForCourse(s.getCourse());
            Map<String, Boolean> studentSessions = s.getSessionAttendance();
            
            for (String key : keys) {
                if (!studentSessions.containsKey(key)) {
                    studentSessions.put(key, false);
                    changed = true;
                }
            }
        }
        if (changed) {
            saveDataToFile();
        }
    }
    
    private Set<String> getPastSessionKeysForCourse(String course) {
        Set<String> keys = new HashSet<>();
        LocalDate today = LocalDate.now();
        LocalDate date = TERM_START_DATE;
        
        ArrayList<ClassSession> sessions = courseTimetables.get(course);
        if (sessions == null) return keys;
        
        while (!date.isAfter(today)) {
            String dayStr = getDayString(date);
            if (!dayStr.isEmpty()) {
                for (ClassSession session : sessions) {
                    if (session.getDay().equals(dayStr)) {
                        String key = date.toString() + "#" + session.getTimeSlot() + "#" + session.getSubject();
                        keys.add(key);
                    }
                }
            }
            date = date.plusDays(1);
        }
        return keys;
    }
    
    private void populatePastSessions() {
        // Just run sync, which dynamically calculates based on current date
        syncGlobalSessions();
    }

    // ================= ASSIGNMENT MANAGEMENT =================

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        saveAssignments();
    }

    public List<Assignment> getAllAssignments() {
        return new ArrayList<>(assignments);
    }

    public List<Assignment> getAssignmentsForSubject(String subject) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : assignments) {
            if (a.getSubject().equalsIgnoreCase(subject)) {
                result.add(a);
            }
        }
        return result;
    }
    
    public Assignment getAssignmentById(String id) {
        for (Assignment a : assignments) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public void addSubmission(AssignmentSubmission submission) {
        // Remove existing submission if any (update it)
        submissions.removeIf(s -> s.getAssignmentId().equals(submission.getAssignmentId()) 
                               && s.getStudentId().equals(submission.getStudentId()));
        submissions.add(submission);
        saveSubmissions();
    }

    public List<AssignmentSubmission> getSubmissionsForAssignment(String assignmentId) {
        List<AssignmentSubmission> result = new ArrayList<>();
        for (AssignmentSubmission s : submissions) {
            if (s.getAssignmentId().equals(assignmentId)) {
                result.add(s);
            }
        }
        return result;
    }

    public AssignmentSubmission getStudentSubmission(String assignmentId, String studentId) {
        for (AssignmentSubmission s : submissions) {
            if (s.getAssignmentId().equals(assignmentId) && s.getStudentId().equals(studentId)) {
                return s;
            }
        }
        return null;
    }
    
    private void saveAssignments() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ASSIGNMENTS_FILE))) {
            for (Assignment a : assignments) {
                writer.println(a.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving assignments: " + e.getMessage());
        }
    }

    private void loadAssignments() {
        File file = new File(ASSIGNMENTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Assignment a = Assignment.fromFileString(line);
                if (a != null) assignments.add(a);
            }
        } catch (IOException e) {
            System.out.println("Error loading assignments: " + e.getMessage());
        }
    }

    private void saveSubmissions() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SUBMISSIONS_FILE))) {
            for (AssignmentSubmission s : submissions) {
                writer.println(s.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving submissions: " + e.getMessage());
        }
    }

    private void loadSubmissions() {
        File file = new File(SUBMISSIONS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                AssignmentSubmission s = AssignmentSubmission.fromFileString(line);
                if (s != null) submissions.add(s);
            }
        } catch (IOException e) {
            System.out.println("Error loading submissions: " + e.getMessage());
        }
    }

    // ================= EVENT MANAGEMENT =================

    public void addEvent(Event event) {
        // If updating existing event
        events.removeIf(e -> e.getId().equals(event.getId()));
        events.add(event);
        saveEvents();
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    public List<Event> getApprovedEvents() {
        List<Event> approved = new ArrayList<>();
        for (Event e : events) {
            if ("APPROVED".equals(e.getStatus())) {
                approved.add(e);
            }
        }
        return approved;
    }

    private void saveEvents() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EVENTS_FILE))) {
            for (Event e : events) {
                writer.println(e.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving events: " + e.getMessage());
        }
    }

    private void loadEvents() {
        File file = new File(EVENTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Event e = Event.fromFileString(line);
                if (e != null) events.add(e);
            }
        } catch (IOException e) {
            System.out.println("Error loading events: " + e.getMessage());
        }
    }

    // ================= EVENT PHOTO MANAGEMENT =================

    public void addEventPhoto(EventPhoto photo) {
        eventPhotos.add(photo);
        saveEventPhotos();
    }

    public List<EventPhoto> getPhotosForEvent(String eventId) {
        List<EventPhoto> result = new ArrayList<>();
        for (EventPhoto p : eventPhotos) {
            if (p.getEventId().equals(eventId)) {
                result.add(p);
            }
        }
        return result;
    }

    public void updateEventPhoto(EventPhoto photo) {
        // Since it's a reference type in the list, just save. 
        // If it was a new object, we'd need to replace.
        // Assuming the caller modified the object from the list.
        // If not, we should find and replace.
        for (int i = 0; i < eventPhotos.size(); i++) {
            EventPhoto p = eventPhotos.get(i);
            if (p.getEventId().equals(photo.getEventId()) && 
                p.getStudentId().equals(photo.getStudentId()) &&
                p.getFilePath().equals(photo.getFilePath())) {
                eventPhotos.set(i, photo);
                break;
            }
        }
        saveEventPhotos();
    }

    private void saveEventPhotos() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EVENT_PHOTOS_FILE))) {
            for (EventPhoto p : eventPhotos) {
                writer.println(p.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving event photos: " + e.getMessage());
        }
    }

    private void loadEventPhotos() {
        File file = new File(EVENT_PHOTOS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                EventPhoto p = EventPhoto.fromFileString(line);
                if (p != null) eventPhotos.add(p);
            }
        } catch (IOException e) {
            System.out.println("Error loading event photos: " + e.getMessage());
        }
    }
}
