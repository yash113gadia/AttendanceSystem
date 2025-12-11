package AttendanceSystem.ui;

import java.awt.*;
import java.awt.event.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Robust and user-friendly Date Picker Dialog.
 */
public class DatePickerDialog extends JDialog {
    
    // State
    private LocalDate selectedDate; // The date the user has clicked
    private YearMonth currentView;  // The month currently displayed
    private boolean confirmed = false;
    
    // UI Components
    private JPanel calendarPanel;
    private JComboBox<String> monthCombo;
    private JSpinner yearSpinner;
    private boolean isProgrammaticChange = false;

    public DatePickerDialog(Window owner, LocalDate initialDate) {
        super(owner, "Select Date", ModalityType.APPLICATION_MODAL);
        
        this.selectedDate = (initialDate != null) ? initialDate : LocalDate.now();
        this.currentView = YearMonth.from(this.selectedDate);
        
        initComponents();
        
        setSize(320, 380);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // 1. Navigation Panel (Month/Year controls)
        JPanel navPanel = new JPanel(new GridBagLayout());
        navPanel.setBackground(DesignSystem.PRIMARY);
        navPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Prev Month Button
        JButton prevBtn = createArrowButton("<");
        prevBtn.addActionListener(e -> {
            currentView = currentView.minusMonths(1);
            updateView();
        });
        
        // Next Month Button
        JButton nextBtn = createArrowButton(">");
        nextBtn.addActionListener(e -> {
            currentView = currentView.plusMonths(1);
            updateView();
        });
        
        // Month Combo
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = Month.of(i+1).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        }
        monthCombo = new JComboBox<>(months);
        monthCombo.setFont(DesignSystem.FONT_BODY_BOLD);
        monthCombo.setFocusable(false);
        monthCombo.addActionListener(e -> {
            if (isProgrammaticChange) return;
            int monthIndex = monthCombo.getSelectedIndex() + 1;
            currentView = currentView.withMonth(monthIndex);
            updateView();
        });
        
        // Year Spinner
        yearSpinner = new JSpinner(new SpinnerNumberModel(currentView.getYear(), 1900, 2100, 1));
        yearSpinner.setFont(DesignSystem.FONT_BODY_BOLD);
        JComponent editor = yearSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setFocusable(false);
        }
        yearSpinner.addChangeListener(e -> {
            if (isProgrammaticChange) return;
            int year = (Integer) yearSpinner.getValue();
            currentView = currentView.withYear(year);
            updateView();
        });
        
        // Layout Navigation
        gbc.weightx = 0.1;
        gbc.gridx = 0; navPanel.add(prevBtn, gbc);
        
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.gridx = 1; navPanel.add(monthCombo, gbc);
        gbc.gridx = 2; navPanel.add(yearSpinner, gbc);
        
        gbc.weightx = 0.1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 3; navPanel.add(nextBtn, gbc);
        
        mainPanel.add(navPanel, BorderLayout.NORTH);
        
        // 2. Calendar Grid
        calendarPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        mainPanel.add(calendarPanel, BorderLayout.CENTER);
        
        // 3. Footer (Buttons)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        styleButton(cancelBtn, Color.WHITE, DesignSystem.TEXT_PRIMARY);
        
        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        styleButton(okBtn, DesignSystem.PRIMARY, Color.WHITE);
        
        footerPanel.add(cancelBtn);
        footerPanel.add(okBtn);
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Initial render
        updateView();
    }
    
    private void updateView() {
        isProgrammaticChange = true;
        monthCombo.setSelectedIndex(currentView.getMonthValue() - 1);
        yearSpinner.setValue(currentView.getYear());
        isProgrammaticChange = false;
        
        calendarPanel.removeAll();
        
        // Weekday Headers
        String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(DesignSystem.FONT_BODY_BOLD);
            lbl.setForeground(DesignSystem.TEXT_SECONDARY);
            calendarPanel.add(lbl);
        }
        
        // Days
        LocalDate firstOfMonth = currentView.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Su=0, Mo=1...
        
        // Padding for previous month
        for (int i = 0; i < dayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        int lengthOfMonth = currentView.lengthOfMonth();
        for (int i = 1; i <= lengthOfMonth; i++) {
            int day = i;
            JButton dayBtn = new JButton(String.valueOf(day));
            LocalDate date = currentView.atDay(day);
            
            // Style
            dayBtn.setFocusPainted(false);
            dayBtn.setBorderPainted(false);
            dayBtn.setOpaque(true);
            
            if (date.equals(selectedDate)) {
                dayBtn.setBackground(DesignSystem.PRIMARY);
                dayBtn.setForeground(Color.WHITE);
            } else if (date.equals(LocalDate.now())) {
                dayBtn.setBackground(DesignSystem.PRIMARY_LIGHT);
                dayBtn.setForeground(DesignSystem.PRIMARY);
            } else {
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setForeground(DesignSystem.TEXT_PRIMARY);
            }
            
            dayBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!date.equals(selectedDate)) {
                        dayBtn.setBackground(DesignSystem.BACKGROUND);
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (!date.equals(selectedDate)) {
                        if (date.equals(LocalDate.now())) {
                            dayBtn.setBackground(DesignSystem.PRIMARY_LIGHT);
                        } else {
                            dayBtn.setBackground(Color.WHITE);
                        }
                    }
                }
            });
            
            dayBtn.addActionListener(e -> {
                selectedDate = date;
                // Re-render to show selection
                updateView();
            });
            
            calendarPanel.add(dayBtn);
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }
    
    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(DesignSystem.FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(DesignSystem.FONT_BUTTON);
        btn.setOpaque(true);
        if (bg.equals(Color.WHITE)) {
            btn.setBorder(BorderFactory.createLineBorder(DesignSystem.BORDER));
        } else {
            btn.setBorderPainted(false);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }
}