package AttendanceSystem;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

// Authentication Manager with persistent user storage
public class AuthenticationManager {
    private static Map<String, User> users = new HashMap<>();
    private static final String USERS_FILE = "users_data.txt";
    private static boolean initialized = false;
    
    static {
        initializeUsers();
    }
    
    private static void initializeUsers() {
        if (initialized) return;
        initialized = true;
        
        // Load saved users first
        loadUsersFromFile();
        
        // Add default admin users if they don't exist
        if (!users.containsKey("YASHGADIA")) {
            users.put("YASHGADIA", new User("YASHGADIA", hashPassword("9v2vcurog"), "ADMIN", new String[]{}));
        }
        if (!users.containsKey("SWETAKUMARI")) {
            users.put("SWETAKUMARI", new User("SWETAKUMARI", hashPassword("ChocoLava"), "ADMIN", new String[]{}));
        }
        
        // Add default teacher users if they don't exist
        addDefaultTeacherIfNotExists("SHM", new String[]{"DSA-I", "DSA-I LAB", "R&P"});
        addDefaultTeacherIfNotExists("RSB", new String[]{"OS", "OS LAB"});
        addDefaultTeacherIfNotExists("DRV", new String[]{"AIC"});
        addDefaultTeacherIfNotExists("AKS", new String[]{"OS LAB"});
        addDefaultTeacherIfNotExists("SWT", new String[]{"DSA-I LAB"});
        addDefaultTeacherIfNotExists("ADM", new String[]{"OOTS"});
        addDefaultTeacherIfNotExists("BNM", new String[]{"TC"});
        addDefaultTeacherIfNotExists("CHC", new String[]{"CAPP"});
        addDefaultTeacherIfNotExists("RSP", new String[]{"AI", "AI LAB"});
        addDefaultTeacherIfNotExists("ANK", new String[]{"DSA-I LAB"});
        addDefaultTeacherIfNotExists("NZM", new String[]{"OS LAB"});
        addDefaultTeacherIfNotExists("DPG", new String[]{"AI LAB"});
        addDefaultTeacherIfNotExists("ASH", new String[]{"INTERNSHIP"});
        addDefaultTeacherIfNotExists("ANU", new String[]{"INTERNSHIP"});
        
        saveUsersToFile();
    }
    
    private static void addDefaultTeacherIfNotExists(String username, String[] subjects) {
        if (!users.containsKey(username)) {
            users.put(username, new User(username, username.toLowerCase() + "123", "TEACHER", subjects));
        }
    }
    
    public static User authenticate(String username, String password) {
        // 1. Check existing Admin/Teacher users
        User user = users.get(username.toUpperCase());
        if (user != null) {
            String storedPass = user.getPassword();
            String inputHash = hashPassword(password);
            
            // Check if stored pass is the hash of input OR (legacy) plain text
            if (storedPass.equals(inputHash)) {
                return user;
            } else if (storedPass.equals(password)) {
                // Auto-migrate legacy plain text to hash
                user.setPassword(inputHash);
                saveUsersToFile();
                return user;
            }
        }
        
        // 2. Check for Student (Simulated Login)
        // Password rule: ID + "123" (e.g., STU001 -> STU001123)
        // We scan attendance_data.txt directly to avoid loading the whole system here.
        File studentFile = new File("attendance_data.txt");
        if (studentFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(studentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // ID|NAME|COURSE...
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String studentId = parts[0];
                        String studentName = parts[1];
                        String course = parts[2];
                        
                        if (studentId.equalsIgnoreCase(username)) {
                            // Validate password
                            String expectedPass = studentId + "123";
                            // Students typically don't have stored hashes in this simple file system yet.
                            // We verify against the rule.
                            if (password.equals(expectedPass)) {
                                User studentUser = new User(studentName, password, "STUDENT", new String[]{course});
                                studentUser.setStudentId(studentId);
                                return studentUser;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error checking student login: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * Check if a teacher exists in the system
     */
    public static boolean teacherExists(String username) {
        return users.containsKey(username.toUpperCase());
    }
    
    /**
     * Add a new teacher with auto-generated password (username + "123")
     * Returns true if a new teacher was created, false if already exists
     */
    public static boolean addTeacherIfNotExists(String username, String subject) {
        String upperUsername = username.toUpperCase().trim();
        if (upperUsername.isEmpty()) return false;
        
        if (users.containsKey(upperUsername)) {
            // Teacher exists, add subject to their list if not already present
            User existingUser = users.get(upperUsername);
            if (existingUser.getRole().equals("TEACHER")) {
                Set<String> subjects = new HashSet<>(Arrays.asList(existingUser.getSubjects()));
                if (!subjects.contains(subject)) {
                    subjects.add(subject);
                    existingUser.setSubjects(subjects.toArray(new String[0]));
                    saveUsersToFile();
                }
            }
            return false;
        }
        
        // Create new teacher with password = username123
        String rawPassword = upperUsername.toLowerCase() + "123";
        User newTeacher = new User(upperUsername, hashPassword(rawPassword), "TEACHER", new String[]{subject});
        users.put(upperUsername, newTeacher);
        saveUsersToFile();
        
        System.out.println("New teacher created: " + upperUsername + " / " + rawPassword);
        return true;
    }
    
    /**
     * Get all registered teachers
     */
    public static String[] getAllTeachers() {
        return users.entrySet().stream()
            .filter(e -> e.getValue().getRole().equals("TEACHER"))
            .map(Map.Entry::getKey)
            .sorted()
            .toArray(String[]::new);
    }
    
    /**
     * Change password for a user
     * Returns true if successful, false if old password doesn't match
     */
    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = users.get(username.toUpperCase());
        if (user == null) return false;
        
        String storedPass = user.getPassword();
        String oldHash = hashPassword(oldPassword);
        
        // Verify old password (check hash OR plain)
        if (!storedPass.equals(oldHash) && !storedPass.equals(oldPassword)) {
            return false;
        }
        
        // Save new password as hash
        user.setPassword(hashPassword(newPassword));
        saveUsersToFile();
        return true;
    }
    
    private static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return password; // Fallback (should not happen)
        }
    }
    
    private static void loadUsersFromFile() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Format: USERNAME|PASSWORD|ROLE|SUBJECT1,SUBJECT2,...
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String username = parts[0];
                    String password = parts[1];
                    String role = parts[2];
                    String[] subjects = parts.length > 3 && !parts[3].isEmpty() 
                        ? parts[3].split(",") 
                        : new String[]{};
                    users.put(username, new User(username, password, role, subjects));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
    
    private static void saveUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, User> entry : users.entrySet()) {
                User user = entry.getValue();
                String subjects = String.join(",", user.getSubjects());
                writer.println(user.getUsername() + "|" + user.getPassword() + "|" + user.getRole() + "|" + subjects);
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}
