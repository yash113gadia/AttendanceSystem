package AttendanceSystem.ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Simple Dropdown Date Picker Dialog.
 */
public class DatePickerDialog extends JDialog {
    
    private LocalDate selectedDate;
    private boolean confirmed = false;
    
    // Components
    private JComboBox<Integer> dayCombo;
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    
    private boolean isUpdating = false;

    public DatePickerDialog(Window owner, LocalDate initialDate) {
        super(owner, "Select Date", ModalityType.APPLICATION_MODAL);
        
        this.selectedDate = (initialDate != null) ? initialDate : LocalDate.now();
        
        initComponents();
        
        // Compact size
        setSize(320, 180);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // --- Selector Panel ---
        JPanel pickerPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        pickerPanel.setBackground(Color.WHITE);
        
        // Labels
        JLabel dLbl = new JLabel("Day");
        dLbl.setFont(DesignSystem.FONT_SMALL);
        dLbl.setForeground(DesignSystem.TEXT_SECONDARY);
        
        JLabel mLbl = new JLabel("Month");
        mLbl.setFont(DesignSystem.FONT_SMALL);
        mLbl.setForeground(DesignSystem.TEXT_SECONDARY);
        
        JLabel yLbl = new JLabel("Year");
        yLbl.setFont(DesignSystem.FONT_SMALL);
        yLbl.setForeground(DesignSystem.TEXT_SECONDARY);
        
        pickerPanel.add(dLbl);
        pickerPanel.add(mLbl);
        pickerPanel.add(yLbl);
        
        // Combos
        dayCombo = new JComboBox<>();
        monthCombo = new JComboBox<>();
        yearCombo = new JComboBox<>();
        
        // Populate Months
        for (Month m : Month.values()) {
            monthCombo.addItem(m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        
        // Populate Years (Current -10 to +10)
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 5; y <= currentYear + 5; y++) {
            yearCombo.addItem(y);
        }
        
        // Setup Listeners
        monthCombo.addActionListener(e -> updateDays());
        yearCombo.addActionListener(e -> updateDays());
        
        // Style Combos
        styleCombo(dayCombo);
        styleCombo(monthCombo);
        styleCombo(yearCombo);
        
        pickerPanel.add(dayCombo);
        pickerPanel.add(monthCombo);
        pickerPanel.add(yearCombo);
        
        mainPanel.add(pickerPanel, BorderLayout.CENTER);
        
        // --- Footer ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        styleButton(cancelBtn, Color.WHITE, DesignSystem.TEXT_PRIMARY);
        
        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> confirmSelection());
        styleButton(okBtn, DesignSystem.PRIMARY, Color.WHITE);
        
        footerPanel.add(cancelBtn);
        footerPanel.add(okBtn);
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Set Initial Values
        setValuesFromDate(selectedDate);
    }
    
    private void setValuesFromDate(LocalDate date) {
        isUpdating = true;
        yearCombo.setSelectedItem(date.getYear());
        monthCombo.setSelectedIndex(date.getMonthValue() - 1);
        isUpdating = false;
        
        updateDays(); // Populate days for this month/year
        
        dayCombo.setSelectedItem(date.getDayOfMonth());
    }
    
    private void updateDays() {
        if (isUpdating) return;
        
        Integer selectedDay = (Integer) dayCombo.getSelectedItem();
        int year = (Integer) yearCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        
        DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>();
        for (int i = 1; i <= daysInMonth; i++) {
            model.addElement(i);
        }
        dayCombo.setModel(model);
        
        // Restore selection if valid, else clamp
        if (selectedDay != null) {
            if (selectedDay <= daysInMonth) {
                dayCombo.setSelectedItem(selectedDay);
            } else {
                dayCombo.setSelectedItem(daysInMonth);
            }
        }
    }
    
    private void confirmSelection() {
        int year = (Integer) yearCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        int day = (Integer) dayCombo.getSelectedItem();
        
        selectedDate = LocalDate.of(year, month, day);
        confirmed = true;
        dispose();
    }
    
    private void styleCombo(JComboBox<?> box) {
        box.setFont(DesignSystem.FONT_BODY);
        box.setBackground(Color.WHITE);
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
        btn.setPreferredSize(new Dimension(80, 30));
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }
}