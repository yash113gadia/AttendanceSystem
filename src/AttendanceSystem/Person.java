package AttendanceSystem;

// Base Person class demonstrating Inheritance
public class Person {
    protected String name;
    protected String id;
    
    public Person(String name, String id) {
        this.name = name;
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getId() {
        return id;
    }
    
    public void displayInfo() {
        System.out.println("Name: " + name + ", ID: " + id);
    }
}
