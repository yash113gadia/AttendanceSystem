package AttendanceSystem;

import AttendanceSystem.ui.DesignSystem;
import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Modern login dialog with clean design.
 */
public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private User loggedInUser = null;
    
    public LoginDialog(JFrame parent) {
        super(parent, "AttendEase - Login", true);
        setSize(420, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        initLoginComponents();
    }
    
    private void initLoginComponents() {
        // Main Container using GridBagLayout for precise 50/50 split
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        // --- LEFT PANEL: Branding & Gradient ---
        JPanel brandingPanel = new JPanel() {
            private Image bgImage = null;
            private boolean imageLoaded = false;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // 1. Always draw Gradient Background
                GradientPaint gp = new GradientPaint(0, 0, DesignSystem.PRIMARY_DARK, 
                    w, h, DesignSystem.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                
                // Decorative Circles
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(-80, -80, 250, 250);
                g2.fillOval(w - 100, h - 100, 200, 200);
                
                // 2. Load Logo Image (Lazy Load)
                if (!imageLoaded) {
                    try {
                        File imgFile = new File("Logo/AttendEaselogo.png");
                        if (imgFile.exists()) {
                            bgImage = javax.imageio.ImageIO.read(imgFile);
                        }
                        imageLoaded = true;
                    } catch (Exception e) {
                        System.out.println("Failed to load logo: " + e.getMessage());
                        imageLoaded = true;
                    }
                }

                // 3. Draw Logo Centered (if loaded)
                if (bgImage != null) {
                    // Scale logo to fit nicely (Increased to 280px for prominence)
                    int targetWidth = 280;
                    double ratio = (double) bgImage.getHeight(null) / bgImage.getWidth(null);
                    int targetHeight = (int) (targetWidth * ratio);
                    
                    int x = (w - targetWidth) / 2;
                    int y = (h - targetHeight) / 2; // Perfectly centered vertically
                    
                    g2.drawImage(bgImage, x, y, targetWidth, targetHeight, null);
                } else {
                    // Fallback Text if logo missing
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 32));
                    String text = "AttendEase";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
                }
                
                g2.dispose();
            }
        };
        // Use GridBagLayout to center contents if we added components, 
        // but we are painting everything now, so just basic is fine.
        brandingPanel.setLayout(new GridBagLayout()); 
        
        // No text labels added here anymore.
        
        // Add Branding to Main (Weight 0.4 = 40% width)
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        mainPanel.add(brandingPanel, gbc);
        
        // --- RIGHT PANEL: Login Form ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout()); // Center form vertically
        formPanel.setBackground(Color.WHITE);
        
        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBackground(Color.WHITE);
        formContent.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
        formContent.setMinimumSize(new Dimension(300, 400));
        
        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 26));
        loginTitle.setForeground(DesignSystem.TEXT_PRIMARY);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContent.add(loginTitle);
        
        formContent.add(Box.createVerticalStrut(5));
        
        JLabel loginSub = new JLabel("Please sign in to continue.");
        loginSub.setFont(DesignSystem.FONT_BODY);
        loginSub.setForeground(DesignSystem.TEXT_SECONDARY);
        loginSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContent.add(loginSub);
        
        formContent.add(Box.createVerticalStrut(30));
        
        // Username
        JLabel userLabel = new JLabel("Username / Student ID");
        userLabel.setFont(DesignSystem.FONT_BODY_BOLD);
        userLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContent.add(userLabel);
        
        formContent.add(Box.createVerticalStrut(8));
        
        usernameField = DesignSystem.createStyledTextField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        formContent.add(usernameField);
        
        formContent.add(Box.createVerticalStrut(20));
        
        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(DesignSystem.FONT_BODY_BOLD);
        passLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContent.add(passLabel);
        
        formContent.add(Box.createVerticalStrut(8));
        
        passwordField = DesignSystem.createStyledPasswordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        formContent.add(passwordField);
        
        formContent.add(Box.createVerticalStrut(30));
        
        // Login Button
        JButton loginButton = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(DesignSystem.PRIMARY_DARK);
                } else if (getModel().isRollover()) {
                    g2.setColor(DesignSystem.PRIMARY.brighter());
                } else {
                    g2.setColor(DesignSystem.PRIMARY);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Text drawing
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        loginButton.setText("Sign In"); // Ensure text is set for custom paint
        loginButton.setFont(DesignSystem.FONT_BUTTON);
        loginButton.setForeground(Color.WHITE);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.addActionListener(e -> performLogin());
        formContent.add(loginButton);
        
        formPanel.add(formContent); // Centered
        
        // Add Form to Main (Weight 0.6 = 60% width)
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        mainPanel.add(formPanel, gbc);
        
        add(mainPanel);
        
        // Set Size
        setSize(850, 500);
        setLocationRelativeTo(getParent());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        loggedInUser = AuthenticationManager.authenticate(username, password);
        
        if (loggedInUser != null) {
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password!", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            usernameField.requestFocus();
        }
    }
    
    public User getLoggedInUser() {
        return loggedInUser;
    }
}
