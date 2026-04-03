package com.cvreviewapp;

import com.cvreviewapp.models.Submission;
import com.cvreviewapp.models.User;
import com.cvreviewapp.services.SubmissionService;
import com.cvreviewapp.utils.Constants;
import com.cvreviewapp.utils.UITheme;
import com.cvreviewapp.utils.CVManager;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Production-ready User Dashboard with state management and rich visual components.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class UserDashboard extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(UserDashboard.class.getName());
    private final SubmissionService submissionService = new SubmissionService();
    private final User user;

    private JTable submissionsTable;
    private SubmissionTableModel tableModel;
    private JProgressBar progressBar;

    public UserDashboard(User user) {
        this.user = user;
        initUI();
        loadSubmissions();
    }

    private void initUI() {
        setTitle(Constants.app.name + " - User Dashboard [" + user.username() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BACKGROUND_COLOR);

        // Sidebar / Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel welcomeLabel = new JLabel("Welcome, " + user.username());
        welcomeLabel.setFont(UITheme.HEADER_FONT);
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(UITheme.ERROR_COLOR);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> {
            new LoginUI();
            dispose();
        });

        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.setOpaque(false);
        content.setBorder(UITheme.MAIN_PADDING);

        // Left Side: History Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        
        tableModel = new SubmissionTableModel();
        submissionsTable = new JTable(tableModel);
        submissionsTable.setRowHeight(40);
        submissionsTable.getTableHeader().setFont(UITheme.REGULAR_FONT);
        submissionsTable.setShowGrid(false);
        submissionsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Custom renderer for status colors
        submissionsTable.setDefaultRenderer(Object.class, new SubmissionRenderer());

        JScrollPane scroll = new JScrollPane(submissionsTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        tablePanel.add(new JLabel("Your Submission History"), BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);
        tablePanel.add(progressBar, BorderLayout.SOUTH);

        // Right Side: Upload Form
        CVUploadPanel uploadForm = new CVUploadPanel(user);
        uploadForm.setPreferredSize(new Dimension(350, 0));
        uploadForm.setMaximumSize(new Dimension(350, Integer.MAX_VALUE));

        content.add(tablePanel);
        content.add(Box.createHorizontalStrut(30));
        content.add(uploadForm);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(content, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    public void loadSubmissions() {
        progressBar.setVisible(true);
        SwingWorker<List<Submission>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Submission> doInBackground() {
                return submissionService.getUserSubmissions(user);
            }

            @Override
            protected void done() {
                try {
                    tableModel.setData(get());
                } catch (Exception e) {
                    LOGGER.severe("Failed to load submissions: " + e.getMessage());
                    tableModel.setData(Collections.emptyList());
                } finally {
                    progressBar.setVisible(false);
                }
            }
        };
        worker.execute();
    }

    private class SubmissionTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Title", "Job Title", "Status", "Date"};
        private List<Submission> data = Collections.emptyList();

        public void setData(List<Submission> data) {
            this.data = data;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Object getValueAt(int row, int col) {
            Submission s = data.get(row);
            return switch (col) {
                case 0 -> s.id();
                case 1 -> s.title();
                case 2 -> s.jobTitle();
                case 3 -> s.status().replace("_", " ");
                case 4 -> s.submissionDate().toString();
                default -> "";
            };
        }
    }

    private class SubmissionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (column == 3) {
                String status = value.toString().toLowerCase();
                if (status.contains("pending")) setForeground(UITheme.SECONDARY_COLOR);
                else if (status.contains("approved")) setForeground(UITheme.SUCCESS_COLOR);
                else if (status.contains("rejected")) setForeground(UITheme.ERROR_COLOR);
            } else {
                setForeground(Color.DARK_GRAY);
            }

            setBackground(isSelected ? new Color(240, 240, 240) : Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return this;
        }
    }
}