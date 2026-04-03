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
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.cvreviewapp.models.Submission;
import com.cvreviewapp.models.User;
import com.cvreviewapp.utils.BackgroundPanel;
import com.cvreviewapp.utils.CVManager;

public class UserDashboard extends JFrame {
    private String username;
    private User user;
    private JLabel statusLabel;
    private JTable submissionsTable;
    private SubmissionTableModel tableModel;
    private static final Logger logger = Logger.getLogger(UserDashboard.class.getName());

    public UserDashboard(String username) {
        if (username == null || username.trim().isEmpty()) {
            Logger.getLogger(UserDashboard.class.getName()).severe("Username is null or empty");
            JOptionPane.showMessageDialog(null, "Invalid username. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        this.username = username;
        this.user = CVManager.getUserByUsername(username);
        setTitle("User Dashboard - " + username);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        if (this.user == null) {
            Logger.getLogger(UserDashboard.class.getName()).severe("User object is null for username: " + username);
            JOptionPane.showMessageDialog(this, "User not found. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

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
        card.setMaximumSize(new Dimension(700, 260));
        card.setMinimumSize(new Dimension(500, 200));
        card.setBorder(new EmptyBorder(32, 32, 32, 32)); // More padding

        // Logo
        JLabel logo = new JLabel();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            java.net.URL bgUrl = getClass().getResource("/com/cvreviewapp/background.jpg");
            if (bgUrl != null) {
                ImageIcon icon = new ImageIcon(bgUrl);
                Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                logo.setIcon(new ImageIcon(img));
            } else {
                System.err.println("[UserDashboard] Resource not found: /com/cvreviewapp/background.jpg");
            }
        } catch (Exception ex) {}
        logo.setBorder(new EmptyBorder(18, 0, 0, 0));

        JLabel title = new JLabel("Welcome, " + username);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(18, 0, 18, 0));
        title.setForeground(new Color(30, 30, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Defensive add method
        java.util.function.Consumer<Component> safeAddToCard = c -> {
            if (c == null) {
                Logger.getLogger(UserDashboard.class.getName()).severe("Attempted to add null component to card panel");
                JOptionPane.showMessageDialog(this, "Internal error: null component in UI.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            card.add(c);
        };

        // Remove old uploadButton and statusLabel
        // Add modern CVUploadPanel
        CVUploadPanel uploadPanel = new CVUploadPanel(user);
        uploadPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        safeAddToCard.accept(uploadPanel);
        // Remove old uploadButton/statusLabel from card

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.RED);
        tableModel = new SubmissionTableModel();
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
        submissionsTable.getColumnModel().getColumn(3).setCellRenderer(new ActionCellRenderer());
        submissionsTable.getColumnModel().getColumn(3).setCellEditor(new ActionCellEditor(submissionsTable));
        // Load data from backend
        loadUserCVs();

        // Add tooltips to table headers
        JTableHeader header = submissionsTable.getTableHeader();
        if (header != null && header.getDefaultRenderer() != null) {
            header.setDefaultRenderer(new HeaderToolTipRenderer(header.getDefaultRenderer()));
        }
        // Card effect for table
        JScrollPane tableScroll = new JScrollPane(submissionsTable);
        tableScroll.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 8, 0, new Color(0,0,0,20)),
            new EmptyBorder(8, 8, 8, 8)
        ));
        tableScroll.setPreferredSize(new Dimension(800, 300));
        tableScroll.setMinimumSize(new Dimension(600, 200));

        safeAddToCard.accept(logo);
        safeAddToCard.accept(title);
        safeAddToCard.accept(Box.createVerticalStrut(10));
        // Remove old uploadButton/statusLabel from card
        safeAddToCard.accept(Box.createVerticalStrut(10));
        safeAddToCard.accept(statusLabel);
        safeAddToCard.accept(Box.createVerticalStrut(10));

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
        // Add Back button to the top of the card
        card.add(Box.createVerticalStrut(10));
        card.add(backButton);
        card.add(Box.createVerticalStrut(10));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        java.util.function.Consumer<Component> safeAddToCenter = c -> {
            if (c == null) {
                Logger.getLogger(UserDashboard.class.getName()).severe("Attempted to add null component to centerPanel");
                JOptionPane.showMessageDialog(this, "Internal error: null component in UI.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            contentPanel.add(c);
        };
        
        safeAddToCenter.accept(Box.createVerticalStrut(20));
        safeAddToCenter.accept(card);
        safeAddToCenter.accept(Box.createVerticalStrut(20));
        safeAddToCenter.accept(tableScroll);
        safeAddToCenter.accept(Box.createVerticalStrut(20));
        
        centerPanel.add(contentPanel, BorderLayout.CENTER);

        bgPanel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(bgPanel);

        setVisible(true);
    }

    public void loadUserCVs() {
        if (user == null) {
            Logger.getLogger(UserDashboard.class.getName()).severe("loadUserCVs called with null user");
            JOptionPane.showMessageDialog(this, "Internal error: user is null. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            tableModel.setData(new ArrayList<>());
            return;
        }
        List<Submission> submissions = CVManager.getUserCVs(user.getId());
        if (submissions == null) {
            Logger.getLogger(UserDashboard.class.getName()).severe("CVManager.getUserCVs returned null for user id " + user.getId());
            JOptionPane.showMessageDialog(this, "Failed to load your CVs. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            tableModel.setData(new ArrayList<>());
            return;
        }
        tableModel.setData(submissions);
    }

    // Remove old handleUpload
    // Remove old loadSubmissions
    // Remove old log
}

// Table model for displaying submissions
class SubmissionTableModel extends javax.swing.table.AbstractTableModel {
    private String[] columns = {"Title", "Job Title", "Status", "Submitted At", "Actions", "Check Result"};
    private List<Submission> submissions = new ArrayList<>();

    public void setData(List<Submission> submissions) {
        this.submissions = submissions != null ? submissions : new ArrayList<>();
        fireTableDataChanged();
    }

    public int getRowCount() { return submissions.size(); }
    public int getColumnCount() { return columns.length; }
    public String getColumnName(int col) { return columns[col]; }
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= submissions.size()) {
            return "";
        }
        Submission s = submissions.get(row);
        if (s == null) {
            return "";
        }
        switch (col) {
            case 0: return s.getFilePath() != null ? new java.io.File(s.getFilePath()).getName() : "";
            case 1: return s.getJobTitle();
            case 2: return s.getStatus() != null ? s.getStatus() : "";
            case 3: return s.getSubmissionDate() != null ? s.getSubmissionDate() : "";
            case 4: return "View | Edit | Delete";
            case 5: return "Check Result";
            default: return "";
        }
    }
    public Submission getSubmissionAt(int row) {
        if (row < 0 || row >= submissions.size()) {
            return null;
        }
        return submissions.get(row);
    }
}

class HeaderToolTipRenderer implements TableCellRenderer {
    private final TableCellRenderer delegate;
    private final String[] tooltips = {
        "The file name of your uploaded CV",
        "Current status of your CV",
        "Date and time you uploaded this CV",
        "Actions: View, Edit, or Delete your CV"
    };
    public HeaderToolTipRenderer(TableCellRenderer delegate) { this.delegate = delegate; }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int column, int row) {
        Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, column, row);
        if (c instanceof JComponent && column >= 0 && column < tooltips.length) {
            ((JComponent) c).setToolTipText(tooltips[column]);
        }
        return c;
    }
}

class ActionCellRenderer extends JPanel implements TableCellRenderer {
    private final JButton viewButton = new JButton();
    private final JButton editButton = new JButton();
    private final JButton deleteButton = new JButton();

    public ActionCellRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        viewButton.setFocusable(true);
        editButton.setFocusable(true);
        deleteButton.setFocusable(true);
        // Set icons
        java.net.URL viewUrl = getClass().getResource("/com/cvreviewapp/icons/view.png");
        if (viewUrl != null) viewButton.setIcon(new ImageIcon(viewUrl));
        else System.err.println("[UserDashboard] Resource not found: /com/cvreviewapp/icons/view.png");
        java.net.URL editUrl = getClass().getResource("/com/cvreviewapp/icons/edit.jpg");
        if (editUrl != null) editButton.setIcon(new ImageIcon(editUrl));
        else System.err.println("[UserDashboard] Resource not found: /com/cvreviewapp/icons/edit.jpg");
        java.net.URL deleteUrl = getClass().getResource("/com/cvreviewapp/icons/delete.png");
        if (deleteUrl != null) deleteButton.setIcon(new ImageIcon(deleteUrl));
        else System.err.println("[UserDashboard] Resource not found: /com/cvreviewapp/icons/delete.png");
        // Set tooltips
        viewButton.setToolTipText("View or download this CV");
        editButton.setToolTipText("Edit title or description");
        deleteButton.setToolTipText("Delete this CV");
        // Flat style
        viewButton.setBorderPainted(false);
        editButton.setBorderPainted(false);
        deleteButton.setBorderPainted(false);
        viewButton.setContentAreaFilled(false);
        editButton.setContentAreaFilled(false);
        deleteButton.setContentAreaFilled(false);
        // Add hover effect
        viewButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { viewButton.setContentAreaFilled(true); }
            public void mouseExited(java.awt.event.MouseEvent evt) { viewButton.setContentAreaFilled(false); }
        });
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { editButton.setContentAreaFilled(true); }
            public void mouseExited(java.awt.event.MouseEvent evt) { editButton.setContentAreaFilled(false); }
        });
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { deleteButton.setContentAreaFilled(true); }
            public void mouseExited(java.awt.event.MouseEvent evt) { deleteButton.setContentAreaFilled(false); }
        });
        // Focus indicator
        viewButton.setFocusPainted(true);
        editButton.setFocusPainted(true);
        deleteButton.setFocusPainted(true);
        viewButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { viewButton.setBorder(new MatteBorder(2,2,2,2, new Color(0,120,215))); }
            public void focusLost(java.awt.event.FocusEvent e) { viewButton.setBorder(null); }
        });
        editButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { editButton.setBorder(new MatteBorder(2,2,2,2, new Color(0,120,215))); }
            public void focusLost(java.awt.event.FocusEvent e) { editButton.setBorder(null); }
        });
        deleteButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { deleteButton.setBorder(new MatteBorder(2,2,2,2, new Color(220,0,0))); }
            public void focusLost(java.awt.event.FocusEvent e) { deleteButton.setBorder(null); }
        });
        add(viewButton);
        add(editButton);
        add(deleteButton);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (row < 0 || table.getRowCount() == 0 || table.getModel() == null) {
            viewButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            setBackground(Color.WHITE);
            return this;
        }
        
        try {
            SubmissionTableModel model = (SubmissionTableModel) table.getModel();
            Submission s = model.getSubmissionAt(row);
            // Fallback for missing icons
            if (viewButton.getIcon() == null) viewButton.setText("View");
            if (editButton.getIcon() == null) editButton.setText("Edit");
            if (deleteButton.getIcon() == null) deleteButton.setText("Delete");
            // Disable buttons if submission is null
            boolean enabled = (s != null);
            viewButton.setEnabled(enabled);
            editButton.setEnabled(enabled);
            deleteButton.setEnabled(enabled);
            if (isSelected) setBackground(new Color(220,240,255));
            else setBackground(Color.WHITE);
        } catch (Exception e) {
            // Fallback in case of any error
            viewButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            setBackground(Color.WHITE);
        }
        return this;
    }
}

class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
    private JPanel panel;
    private JButton viewButton, editButton, deleteButton;
    private JTable table;

    public ActionCellEditor(JTable table) {
        this.table = table;
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        viewButton = new JButton();
        editButton = new JButton();
        deleteButton = new JButton();
        // Set icons
        java.net.URL viewUrl2 = getClass().getResource("/com/cvreviewapp/icons/view.png");
        if (viewUrl2 != null) viewButton.setIcon(new ImageIcon(viewUrl2));
        else System.err.println("[UserDashboard] Resource not found: /com/cvreviewapp/icons/view.png");
        java.net.URL editUrl2 = getClass().getResource("/com/cvreviewapp/icons/edit.jpg");
        if (editUrl2 != null) editButton.setIcon(new ImageIcon(editUrl2));
        else System.err.println("[UserDashboard] Resource not found: /com/cvreviewapp/icons/edit.jpg");
        java.net.URL deleteUrl2 = getClass().getResource("/com/cvreviewapp/icons/delete.png");
        if (deleteUrl2 != null) deleteButton.setIcon(new ImageIcon(deleteUrl2));
        else System.err.println("[UserDashboard] Resource not found: /com/cvreviewapp/icons/delete.png");
        // Set tooltips
        viewButton.setToolTipText("View or download this CV");
        editButton.setToolTipText("Edit title or description");
        deleteButton.setToolTipText("Delete this CV");
        // Flat style
        viewButton.setBorderPainted(false);
        editButton.setBorderPainted(false);
        deleteButton.setBorderPainted(false);
        viewButton.setContentAreaFilled(false);
        editButton.setContentAreaFilled(false);
        deleteButton.setContentAreaFilled(false);
        // Add hover effect
        viewButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { viewButton.setContentAreaFilled(true); }
            public void mouseExited(java.awt.event.MouseEvent evt) { viewButton.setContentAreaFilled(false); }
        });
        editButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { editButton.setContentAreaFilled(true); }
            public void mouseExited(java.awt.event.MouseEvent evt) { editButton.setContentAreaFilled(false); }
        });
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { deleteButton.setContentAreaFilled(true); }
            public void mouseExited(java.awt.event.MouseEvent evt) { deleteButton.setContentAreaFilled(false); }
        });
        // Focus indicator
        viewButton.setFocusPainted(true);
        editButton.setFocusPainted(true);
        deleteButton.setFocusPainted(true);
        viewButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { viewButton.setBorder(new MatteBorder(2,2,2,2, new Color(0,120,215))); }
            public void focusLost(java.awt.event.FocusEvent e) { viewButton.setBorder(null); }
        });
        editButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { editButton.setBorder(new MatteBorder(2,2,2,2, new Color(0,120,215))); }
            public void focusLost(java.awt.event.FocusEvent e) { editButton.setBorder(null); }
        });
        deleteButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { deleteButton.setBorder(new MatteBorder(2,2,2,2, new Color(220,0,0))); }
            public void focusLost(java.awt.event.FocusEvent e) { deleteButton.setBorder(null); }
        });
        panel.add(viewButton);
        panel.add(editButton);
        panel.add(deleteButton);

        viewButton.addActionListener(e -> handleView());
        editButton.addActionListener(e -> handleEdit());
        deleteButton.addActionListener(e -> handleDelete());
    }

    private void handleView() {
        int row = table.getEditingRow();
        if (row < 0 || table.getRowCount() == 0 || table.getModel() == null) {
            fireEditingStopped();
            return;
        }
        try {
            SubmissionTableModel model = (SubmissionTableModel) table.getModel();
            Submission s = model.getSubmissionAt(row);
            if (s == null) {
                fireEditingStopped();
                return;
            }
            if (s.getFilePath() != null) {
                try {
                    Desktop.getDesktop().open(new java.io.File(s.getFilePath()));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(table, "Failed to open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(table, "Error handling view action: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        fireEditingStopped();
    }

    private void handleEdit() {
        int row = table.getEditingRow();
        if (row < 0 || table.getRowCount() == 0 || table.getModel() == null) {
            fireEditingStopped();
            return;
        }
        try {
            SubmissionTableModel model = (SubmissionTableModel) table.getModel();
            Submission s = model.getSubmissionAt(row);
            if (s == null) {
                fireEditingStopped();
                return;
            }
            String currentTitle = s.getFilePath() != null ? new java.io.File(s.getFilePath()).getName() : "";
            String newTitle = JOptionPane.showInputDialog(table, "Edit Title:", currentTitle);
            String newDesc = JOptionPane.showInputDialog(table, "Edit Description:", "");
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                boolean updated = com.cvreviewapp.utils.CVManager.updateCVMetadata(s.getId(), s.getUserId(), newTitle, newDesc);
                if (updated) {
                    JOptionPane.showMessageDialog(table, "CV metadata updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    ((UserDashboard) SwingUtilities.getWindowAncestor(table)).loadUserCVs();
                } else {
                    JOptionPane.showMessageDialog(table, "Failed to update CV metadata.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(table, "Error handling edit action: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        fireEditingStopped();
    }

    private void handleDelete() {
        int row = table.getEditingRow();
        if (row < 0 || table.getRowCount() == 0 || table.getModel() == null) {
            fireEditingStopped();
            return;
        }
        try {
            SubmissionTableModel model = (SubmissionTableModel) table.getModel();
            Submission s = model.getSubmissionAt(row);
            if (s == null) {
                fireEditingStopped();
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(table, "Are you sure you want to delete this CV?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = com.cvreviewapp.utils.CVManager.deleteCV(s.getId(), s.getUserId());
                if (deleted) {
                    JOptionPane.showMessageDialog(table, "CV deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    ((UserDashboard) SwingUtilities.getWindowAncestor(table)).loadUserCVs();
                } else {
                    JOptionPane.showMessageDialog(table, "Failed to delete CV.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(table, "Error handling delete action: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        fireEditingStopped();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }
} 