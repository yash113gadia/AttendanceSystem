package AttendanceSystem.ui;

import AttendanceSystem.*;
import javax.swing.*;
import java.awt.*;

/**
 * Base class for all feature panels.
 * Extend this class to create new feature panels.
 */
public abstract class BasePanel extends JPanel {
    protected AttendanceSystem system;
    protected User currentUser;
    protected Runnable onDataChanged;
    
    public BasePanel(AttendanceSystem system, User currentUser) {
        this.system = system;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    /**
     * Set a callback to be called when data changes (e.g., student added)
     */
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }
    
    /**
     * Notify that data has changed
     */
    protected void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
    
    /**
     * Called when this panel becomes visible
     */
    public void onShow() {
        // Override in subclasses if needed
    }
    
    /**
     * Called when this panel is hidden
     */
    public void onHide() {
        // Override in subclasses if needed
    }
    
    /**
     * Check if current user is admin
     */
    protected boolean isAdmin() {
        return currentUser.getRole().equals("ADMIN");
    }
    
    /**
     * Show error message dialog
     */
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show success message dialog
     */
    protected void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show info message dialog
     */
    protected void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
