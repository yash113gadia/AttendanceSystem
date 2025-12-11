package AttendanceSystem.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for common UI components and styling.
 * Use these methods to maintain consistent look and feel.
 */
public class UIUtils {
    
    // Color constants
    public static final Color PRIMARY_BLUE = new Color(41, 128, 185);
    public static final Color SECONDARY_BLUE = new Color(52, 152, 219);
    public static final Color LIGHT_GRAY = new Color(236, 240, 241);
    public static final Color DARK_GRAY = new Color(44, 62, 80);
    public static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    public static final Color DANGER_RED = new Color(231, 76, 60);
    public static final Color WARNING_ORANGE = new Color(255, 140, 0);
    
    /**
     * Create a styled button with consistent appearance
     */
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /**
     * Create a toggle button with consistent styling
     */
    public static JToggleButton createStyledToggleButton(String text) {
        JToggleButton button = new JToggleButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(100, 45));
        button.setBackground(LIGHT_GRAY);
        button.setForeground(DARK_GRAY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(SECONDARY_BLUE);
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(LIGHT_GRAY);
                button.setForeground(DARK_GRAY);
            }
        });
        
        return button;
    }
    
    /**
     * Create a titled border panel
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SECONDARY_BLUE, 2),
            title,
            0,
            0,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_BLUE
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    /**
     * Create a section header label
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    /**
     * Create a standard text field
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }
    
    /**
     * Create a standard label
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        return label;
    }
    
    /**
     * Create a scrollable text area for displaying information
     */
    public static JScrollPane createScrollableTextArea(String content, int width, int height) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(width, height));
        return scrollPane;
    }
}
