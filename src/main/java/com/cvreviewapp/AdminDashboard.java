package com.cvreviewapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.cvreviewapp.models.Submission;
import com.cvreviewapp.utils.BackgroundPanel;
import com.cvreviewapp.utils.CVManager;

public class AdminDashboard extends JFrame {
    private JTable submissionsTable;
    private AdminSubmissionTableModel tableModel;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(new BorderLayout());

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Drop shadow
                g2.setColor(new Color(0,0,0,40));
                g2.fillRoundRect(12, 12, getWidth()-24, getHeight()-24, 36, 36);
                // Main card
                g2.setColor(new Color(255,255,255,245)); // More opaque white
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new LineBorder(new Color(180,180,180,180), 2, true));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 245)); // More opaque card
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setAlignmentY(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(900, 260));
        card.setMinimumSize(new Dimension(700, 200));
        card.setBorder(new EmptyBorder(32, 32, 32, 32)); // More padding

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(24, 0, 24, 0));
        title.setForeground(new Color(30, 30, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        tableModel = new AdminSubmissionTableModel();
        submissionsTable = new JTable(tableModel);
        submissionsTable.setShowGrid(true);
        submissionsTable.setGridColor(new Color(220,220,220));
        submissionsTable.setRowHeight(32);
        submissionsTable.setSelectionBackground(new Color(220,240,255));
        submissionsTable.setSelectionForeground(Color.BLACK);
        submissionsTable.setFillsViewportHeight(true);
        submissionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        submissionsTable.getTableHeader().setBackground(new Color(240,240,240));
        submissionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        submissionsTable.getColumnModel().getColumn(4).setCellRenderer(new AdminActionCellRenderer());
        submissionsTable.getColumnModel().getColumn(4).setCellEditor(new AdminActionCellEditor(submissionsTable, this));

        // Add a MouseListener to the table to handle Check CV button clicks
        submissionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = submissionsTable.rowAtPoint(e.getPoint());
                int col = submissionsTable.columnAtPoint(e.getPoint());
                if (col == 6 && row >= 0) { // Check CV column
                    Submission s = tableModel.getSubmissionAt(row);
                    if (s != null && s.getJobTitle() != null && s.getFilePath() != null) {
                        java.util.Map<String, Object> result = com.cvreviewapp.utils.CVManager.checkCV(s.getFilePath(), s.getJobTitle());
                        StringBuilder msg = new StringBuilder();
                        if (result.containsKey("error")) {
                            msg.append(result.get("error"));
                        } else {
                            msg.append("Job Title: ").append(result.get("jobTitle")).append("\n");
                            msg.append("\nMissing Skills: ").append(result.get("missingSkills")).append("\n");
                            msg.append("Qualification Present: ").append(result.get("hasQualification")).append("\n");
                            msg.append("Experience Present: ").append(result.get("hasExperience")).append("\n");
                        }
                        javax.swing.JOptionPane.showMessageDialog(AdminDashboard.this, msg.toString(), "CV Check Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(submissionsTable);
        tableScroll.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Search/filter bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setOpaque(false);
        String[] statuses = {"", "PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED", "ARCHIVED"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);
        statusBox.setToolTipText("Filter by status");
        JTextField userIdField = new JTextField(6);
        userIdField.setToolTipText("Filter by user ID");
        JTextField keywordField = new JTextField(10);
        keywordField.setToolTipText("Search by title or description");
        JTextField fromDateField = new JTextField(8);
        fromDateField.setToolTipText("From date (yyyy-MM-dd)");
        JTextField toDateField = new JTextField(8);
        toDateField.setToolTipText("To date (yyyy-MM-dd)");
        JButton searchBtn = new JButton("Search");
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusBox);
        filterPanel.add(new JLabel("User ID:"));
        filterPanel.add(userIdField);
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(fromDateField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(toDateField);
        filterPanel.add(new JLabel("Keyword:"));
        filterPanel.add(keywordField);
        filterPanel.add(searchBtn);
        // Add filterPanel to card
        card.add(filterPanel, 1);
        // Search button action
        searchBtn.addActionListener(e -> {
            String status = statusBox.getSelectedItem() != null && !statusBox.getSelectedItem().toString().isEmpty() ? statusBox.getSelectedItem().toString() : null;
            Integer userId = null;
            try { if (!userIdField.getText().trim().isEmpty()) userId = Integer.parseInt(userIdField.getText().trim()); } catch (Exception ex) {}
            String keyword = keywordField.getText().trim().isEmpty() ? null : keywordField.getText().trim();
            Timestamp fromDate = null, toDate = null;
            try { if (!fromDateField.getText().trim().isEmpty()) fromDate = Timestamp.valueOf(fromDateField.getText().trim() + " 00:00:00"); } catch (Exception ex) {}
            try { if (!toDateField.getText().trim().isEmpty()) toDate = Timestamp.valueOf(toDateField.getText().trim() + " 23:59:59"); } catch (Exception ex) {}
            java.util.List<Submission> results = com.cvreviewapp.utils.CVManager.searchCVs(status, userId, keyword, fromDate, toDate, 100, 0);
            tableModel.setData(results);
        });

        // Add Back button
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.setBackground(new Color(220, 220, 220));
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            new HomePage();
            dispose();
        });
        card.add(Box.createVerticalStrut(10));
        card.add(backButton);
        card.add(Box.createVerticalStrut(10));

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(tableScroll);
        card.add(Box.createVerticalStrut(10));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(card);
        centerPanel.add(Box.createVerticalStrut(20));

        bgPanel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(bgPanel);

        loadAllSubmissions();
        setVisible(true);
    }

    public void loadAllSubmissions() {
        List<Submission> submissions = CVManager.getAllCVs(null, null, 100, 0);
        tableModel.setData(submissions);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}

class AdminSubmissionTableModel extends AbstractTableModel {
    private String[] columns = {"Title", "User ID", "Job Title", "Status", "Submitted At", "Actions", "Check CV"};
    private java.util.List<Submission> submissions = new java.util.ArrayList<>();

    public void setData(List<Submission> submissions) {
        this.submissions = submissions != null ? submissions : new java.util.ArrayList<>();
        fireTableDataChanged();
    }

    public int getRowCount() { return submissions.size(); }
    public int getColumnCount() { return columns.length; }
    public String getColumnName(int col) { return columns[col]; }
    public Object getValueAt(int row, int col) {
        Submission s = submissions.get(row);
        switch (col) {
            case 0: return s.getFilePath() != null ? new java.io.File(s.getFilePath()).getName() : "";
            case 1: return s.getUserId();
            case 2: return s.getJobTitle();
            case 3: return s.getStatus();
            case 4: return s.getSubmissionDate();
            case 5: return "View | Edit | Delete | Review";
            case 6: return "Check CV";
            default: return "";
        }
    }
    public Submission getSubmissionAt(int row) {
        return submissions.get(row);
    }
}

class AdminActionCellRenderer extends JPanel implements TableCellRenderer {
    private final JButton viewButton = new JButton();
    private final JButton editButton = new JButton();
    private final JButton deleteButton = new JButton();
    private final JButton reviewButton = new JButton();
    private final JButton statusButton = new JButton();

    public AdminActionCellRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
        viewButton.setText("View");
        editButton.setText("Edit");
        deleteButton.setText("Delete");
        reviewButton.setText("Review");
        statusButton.setText("Status");
        viewButton.setToolTipText("View or download this CV");
        editButton.setToolTipText("Edit title or description");
        deleteButton.setToolTipText("Delete this CV");
        reviewButton.setToolTipText("Add review notes and scores");
        statusButton.setToolTipText("Change status (approve, reject, archive)");
        viewButton.setFocusable(true);
        editButton.setFocusable(true);
        deleteButton.setFocusable(true);
        reviewButton.setFocusable(true);
        statusButton.setFocusable(true);
        add(viewButton);
        add(editButton);
        add(deleteButton);
        add(reviewButton);
        add(statusButton);
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) setBackground(new Color(220,240,255));
        else setBackground(Color.WHITE);
        return this;
    }
}

class AdminActionCellEditor extends AbstractCellEditor implements TableCellEditor {
    private JPanel panel;
    private JButton viewButton, editButton, deleteButton, reviewButton, statusButton;
    private JTable table;
    private AdminDashboard dashboard;

    public AdminActionCellEditor(JTable table, AdminDashboard dashboard) {
        this.table = table;
        this.dashboard = dashboard;
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        viewButton = new JButton("View");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        reviewButton = new JButton("Review");
        statusButton = new JButton("Status");
        viewButton.setToolTipText("View or download this CV");
        editButton.setToolTipText("Edit title or description");
        deleteButton.setToolTipText("Delete this CV");
        reviewButton.setToolTipText("Add review notes and scores");
        statusButton.setToolTipText("Change status (approve, reject, archive)");
        panel.add(viewButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(reviewButton);
        panel.add(statusButton);
        viewButton.addActionListener(e -> handleView());
        editButton.addActionListener(e -> handleEdit());
        deleteButton.addActionListener(e -> handleDelete());
        reviewButton.addActionListener(e -> handleReview());
        statusButton.addActionListener(e -> handleStatus());
    }
    private void handleView() {
        int row = table.getEditingRow();
        AdminSubmissionTableModel model = (AdminSubmissionTableModel) table.getModel();
        Submission s = model.getSubmissionAt(row);
        if (s.getFilePath() != null) {
            try {
                Desktop.getDesktop().open(new java.io.File(s.getFilePath()));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(table, "Failed to open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        fireEditingStopped();
    }
    private void handleEdit() {
        int row = table.getEditingRow();
        AdminSubmissionTableModel model = (AdminSubmissionTableModel) table.getModel();
        Submission s = model.getSubmissionAt(row);
        String newTitle = JOptionPane.showInputDialog(table, "Edit Title:", new java.io.File(s.getFilePath()).getName());
        String newDesc = JOptionPane.showInputDialog(table, "Edit Description:", "");
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            boolean updated = com.cvreviewapp.utils.CVManager.updateCVMetadata(s.getId(), s.getUserId(), newTitle, newDesc);
            if (updated) {
                JOptionPane.showMessageDialog(table, "CV metadata updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dashboard.loadAllSubmissions();
            } else {
                JOptionPane.showMessageDialog(table, "Failed to update CV metadata.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        fireEditingStopped();
    }
    private void handleDelete() {
        int row = table.getEditingRow();
        AdminSubmissionTableModel model = (AdminSubmissionTableModel) table.getModel();
        Submission s = model.getSubmissionAt(row);
        int confirm = JOptionPane.showConfirmDialog(table, "Are you sure you want to delete this CV?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = com.cvreviewapp.utils.CVManager.deleteCV(s.getId(), s.getUserId());
            if (deleted) {
                JOptionPane.showMessageDialog(table, "CV deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dashboard.loadAllSubmissions();
            } else {
                JOptionPane.showMessageDialog(table, "Failed to delete CV.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        fireEditingStopped();
    }
    private void handleReview() {
        int row = table.getEditingRow();
        AdminSubmissionTableModel model = (AdminSubmissionTableModel) table.getModel();
        Submission s = model.getSubmissionAt(row);
        String scoreStr = JOptionPane.showInputDialog(table, "Enter review score (0-10):", "");
        String comments = JOptionPane.showInputDialog(table, "Enter review comments:", "");
        try {
            double score = Double.parseDouble(scoreStr);
            boolean reviewed = com.cvreviewapp.utils.CVManager.addReviewScore(s.getId(), 1, 1, score, comments); // reviewerId/criteriaId demo
            if (reviewed) {
                JOptionPane.showMessageDialog(table, "Review saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(table, "Failed to save review.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(table, "Invalid score.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        fireEditingStopped();
    }
    private void handleStatus() {
        int row = table.getEditingRow();
        AdminSubmissionTableModel model = (AdminSubmissionTableModel) table.getModel();
        Submission s = model.getSubmissionAt(row);
        String[] statuses = {"PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED", "ARCHIVED"};
        String newStatus = (String) JOptionPane.showInputDialog(table, "Change status:", "Status", JOptionPane.QUESTION_MESSAGE, null, statuses, s.getStatus());
        String notes = JOptionPane.showInputDialog(table, "Review notes (optional):", "");
        if (newStatus != null) {
            boolean updated = com.cvreviewapp.utils.CVManager.updateCVStatus(s.getId(), newStatus, 1, notes); // reviewerId demo
            if (updated) {
                JOptionPane.showMessageDialog(table, "Status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dashboard.loadAllSubmissions();
            } else {
                JOptionPane.showMessageDialog(table, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        fireEditingStopped();
    }
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return panel;
    }
    @Override
    public Object getCellEditorValue() { return null; }
} 