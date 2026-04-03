package com.cvreviewapp;

import com.cvreviewapp.models.Submission;
import com.cvreviewapp.services.SubmissionService;
import com.cvreviewapp.utils.Constants;
import com.cvreviewapp.utils.UITheme;
import com.cvreviewapp.utils.CVManager;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Production-ready Admin Dashboard with comprehensive submission management and status controls.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class AdminDashboard extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboard.class.getName());
    private final SubmissionService submissionService = new SubmissionService();

    private JTable submissionsTable;
    private AdminSubmissionTableModel tableModel;
    private JProgressBar progressBar;

    public AdminDashboard() {
        initUI();
        loadAllSubmissions();
    }

    private void initUI() {
        setTitle(Constants.app.name + " - Administrative Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BACKGROUND_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80)); // Darker slate for admin
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel titleLabel = new JLabel("System Administration");
        titleLabel.setFont(UITheme.HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(UITheme.ERROR_COLOR);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> {
            new LoginUI();
            dispose();
        });

        header.add(titleLabel, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);

        // Content
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(UITheme.MAIN_PADDING);

        // Submissions Table
        tableModel = new AdminSubmissionTableModel();
        submissionsTable = new JTable(tableModel);
        submissionsTable.setRowHeight(45);
        submissionsTable.getTableHeader().setFont(UITheme.REGULAR_FONT);
        submissionsTable.setShowGrid(false);
        submissionsTable.setDefaultRenderer(Object.class, new AdminSubmissionRenderer());

        JScrollPane scroll = new JScrollPane(submissionsTable);
        scroll.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        // Control Panel
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setOpaque(false);
        
        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.addActionListener(e -> loadAllSubmissions());
        
        JButton approveBtn = new JButton("Approve Selected");
        approveBtn.setBackground(UITheme.SUCCESS_COLOR);
        approveBtn.setForeground(Color.WHITE);
        approveBtn.addActionListener(e -> updateSelectedStatus("APPROVED"));

        JButton rejectBtn = new JButton("Reject Selected");
        rejectBtn.setBackground(UITheme.ERROR_COLOR);
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.addActionListener(e -> updateSelectedStatus("REJECTED"));

        controls.add(refreshBtn);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(approveBtn);
        controls.add(rejectBtn);

        content.add(new JLabel("All System Submissions"), BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(controls, BorderLayout.SOUTH);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(content, BorderLayout.CENTER);
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void loadAllSubmissions() {
        progressBar.setVisible(true);
        SwingWorker<List<Submission>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Submission> doInBackground() {
                return submissionService.getAllSubmissions();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setData(get());
                } catch (Exception e) {
                    LOGGER.severe("Admin load failed: " + e.getMessage());
                    tableModel.setData(Collections.emptyList());
                } finally {
                    progressBar.setVisible(false);
                }
            }
        };
        worker.execute();
    }

    private void updateSelectedStatus(String status) {
        int row = submissionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a submission first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Submission s = tableModel.getSubmissionAt(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Update status of CV '" + s.title() + "' to " + status + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (submissionService.updateStatus(s.id(), status)) {
                CVManager.logAction(null, "ADMIN_STATUS_UPDATE", "Updated CV " + s.id() + " to " + status);
                loadAllSubmissions();
            }
        }
    }

    private class AdminSubmissionTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "User", "Title", "Job Title", "Status", "Date"};
        private List<Submission> data = Collections.emptyList();

        public void setData(List<Submission> data) { this.data = data; fireTableDataChanged(); }
        public Submission getSubmissionAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Object getValueAt(int row, int col) {
            Submission s = data.get(row);
            return switch (col) {
                case 0 -> s.id();
                case 1 -> s.userId(); // Should ideally show username
                case 2 -> s.title();
                case 3 -> s.jobTitle();
                case 4 -> s.status();
                case 5 -> s.submissionDate().toString();
                default -> "";
            };
        }
    }

    private class AdminSubmissionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 4) {
                String s = value.toString().toLowerCase();
                if (s.contains("approved")) setForeground(UITheme.SUCCESS_COLOR);
                else if (s.contains("rejected")) setForeground(UITheme.ERROR_COLOR);
                else setForeground(UITheme.SECONDARY_COLOR);
            } else setForeground(Color.DARK_GRAY);
            setBackground(isSelected ? new Color(230, 240, 250) : Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return this;
        }
    }
}