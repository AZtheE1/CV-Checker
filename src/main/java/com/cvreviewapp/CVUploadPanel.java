package com.cvreviewapp;

import com.cvreviewapp.models.Submission;
import com.cvreviewapp.models.User;
import com.cvreviewapp.services.SubmissionService;
import com.cvreviewapp.utils.CVManager;
import com.cvreviewapp.utils.Constants;
import com.cvreviewapp.utils.UITheme;
import java.awt.*;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Production-ready CV Upload Component with async processing and live feedback.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class CVUploadPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(CVUploadPanel.class.getName());
    private final SubmissionService submissionService = new SubmissionService();
    private final User user;

    private JTextField titleField;
    private JComboBox<String> jobTitleBox;
    private JTextArea descriptionArea;
    private JLabel fileLabel;
    private JProgressBar progressBar;
    private JButton uploadButton;
    private File selectedFile;

    public CVUploadPanel(User user) {
        this.user = user;
        initUI();
    }

    private void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.SECONDARY_COLOR, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Submit New CV");
        title.setFont(UITheme.HEADER_FONT);
        title.setForeground(UITheme.PRIMARY_COLOR);

        titleField = createTextField();
        jobTitleBox = new JComboBox<>(new String[]{"Java Developer", "Frontend Developer", "Data Scientist", "Project Manager"}); // Placeholder, should be from DB
        jobTitleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        descriptionArea.setLineWrap(true);

        fileLabel = new JLabel("No file selected");
        fileLabel.setFont(UITheme.SMALL_FONT);

        JButton browseButton = new JButton("Select PDF");
        browseButton.addActionListener(e -> selectFile());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        uploadButton = new JButton("Upload & Analyze");
        uploadButton.setBackground(UITheme.PRIMARY_COLOR);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFont(UITheme.HEADER_FONT);
        uploadButton.addActionListener(e -> handleUpload());

        add(title);
        add(Box.createVerticalStrut(15));
        add(new JLabel("CV Title"));
        add(titleField);
        add(Box.createVerticalStrut(10));
        add(new JLabel("Applying For"));
        add(jobTitleBox);
        add(Box.createVerticalStrut(10));
        add(new JLabel("Notes (Optional)"));
        add(new JScrollPane(descriptionArea));
        add(Box.createVerticalStrut(15));
        add(fileLabel);
        add(browseButton);
        add(Box.createVerticalStrut(20));
        add(progressBar);
        add(uploadButton);
    }

    private JTextField createTextField() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        return f;
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents", "pdf"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileLabel.setText("Selected: " + selectedFile.getName());
        }
    }

    private void handleUpload() {
        String title = titleField.getText().trim();
        String jobTitle = (String) jobTitleBox.getSelectedItem();
        
        if (title.isEmpty() || selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Title and File are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setLoading(true);
        SwingWorker<Submission, Void> worker = new SwingWorker<>() {
            @Override
            protected Submission doInBackground() throws Exception {
                return submissionService.submitCV(user, selectedFile, title, descriptionArea.getText(), jobTitle);
            }

            @Override
            protected void done() {
                try {
                    Submission s = get();
                    if (s != null) {
                        CVManager.logAction(user.id(), "CV_UPLOAD", "Uploaded CV: " + title);
                        showAnalysisResult(s, jobTitle);
                        clearForm();
                    }
                } catch (Exception e) {
                    LOGGER.severe("Upload failed: " + e.getMessage());
                    JOptionPane.showMessageDialog(CVUploadPanel.this, "Upload failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setLoading(false);
                }
            }
        };
        worker.execute();
    }

    private void showAnalysisResult(Submission s, String jobTitle) {
        var result = CVManager.checkCV(s.filePath(), jobTitle);
        StringBuilder msg = new StringBuilder("Analysis for ").append(jobTitle).append(":\n\n");
        if (result.containsKey("error")) {
            msg.append("Error: ").append(result.get("error"));
        } else {
            msg.append("Missing Skills: ").append(result.get("missingSkills")).append("\n");
            msg.append("Qualification Match: ").append(((boolean)result.get("hasQualification") ? "Yes" : "No")).append("\n");
            msg.append("Experience Match: ").append(((boolean)result.get("hasExperience") ? "Yes" : "No")).append("\n");
        }
        JOptionPane.showMessageDialog(this, msg.toString(), "CV Analysis Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        uploadButton.setEnabled(!loading);
    }

    private void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        selectedFile = null;
        fileLabel.setText("No file selected");
    }
}