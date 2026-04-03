package com.cvreviewapp;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.cvreviewapp.models.Submission;
import com.cvreviewapp.models.User;
import com.cvreviewapp.utils.CVManager;

public class CVUploadPanel extends JPanel {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JTextField fileField;
    private JButton browseButton;
    private JButton uploadButton;
    private JLabel feedbackLabel;
    private File selectedFile;
    private User user;
    private JComboBox<String> jobTitleBox;

    public CVUploadPanel(User user) {
        this.user = user;
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel titleLabel = new JLabel("Upload Your CV");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        card.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel fileLabel = new JLabel("PDF File:");
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        card.add(fileLabel, gbc);

        gbc.gridx = 1;
        fileField = new JTextField(18);
        fileField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        fileField.setEditable(false);
        card.add(fileField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        browseButton = new JButton("Browse");
        browseButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        browseButton.setToolTipText("Select a PDF file to upload");
        card.add(browseButton, gbc);

        gbc.gridx = 1;
        uploadButton = new JButton("Upload");
        uploadButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        uploadButton.setToolTipText("Upload the selected CV");
        uploadButton.setEnabled(false);
        card.add(uploadButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel titleLbl = new JLabel("Title:");
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        card.add(titleLbl, gbc);

        gbc.gridx = 1;
        titleField = new JTextField(18);
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        card.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel jobTitleLbl = new JLabel("Job Title:");
        jobTitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        card.add(jobTitleLbl, gbc);

        gbc.gridx = 1;
        jobTitleBox = new JComboBox<>();
        for (com.cvreviewapp.utils.CVManager.JobRequirement req : com.cvreviewapp.utils.CVManager.JOB_REQUIREMENTS) {
            jobTitleBox.addItem(req.jobTitle);
        }
        jobTitleBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        card.add(jobTitleBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel descLbl = new JLabel("Description:");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        card.add(descLbl, gbc);

        gbc.gridx = 1;
        descriptionArea = new JTextArea(3, 18);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        card.add(descScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        feedbackLabel.setForeground(new Color(60, 120, 60));
        card.add(feedbackLabel, gbc);

        // Add card to main panel
        add(card);

        // Actions
        browseButton.addActionListener(this::onBrowse);
        uploadButton.addActionListener(this::onUpload);
    }

    private void onBrowse(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileField.setText(selectedFile.getName());
            uploadButton.setEnabled(selectedFile.getName().toLowerCase().endsWith(".pdf"));
            feedbackLabel.setText(" ");
        }
    }

    private void onUpload(ActionEvent e) {
        if (selectedFile == null || !selectedFile.getName().toLowerCase().endsWith(".pdf")) {
            feedbackLabel.setText("Please select a valid PDF file.");
            feedbackLabel.setForeground(Color.RED);
            return;
        }
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        if (title.isEmpty()) {
            feedbackLabel.setText("Title is required.");
            feedbackLabel.setForeground(Color.RED);
            return;
        }
        String jobTitle = (String) jobTitleBox.getSelectedItem();
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            feedbackLabel.setText("Please select a job title.");
            feedbackLabel.setForeground(Color.RED);
            return;
        }
        if (user == null) {
            feedbackLabel.setText("User not found. Please re-login.");
            feedbackLabel.setForeground(Color.RED);
            return;
        }
        feedbackLabel.setText("Uploading...");
        feedbackLabel.setForeground(new Color(60, 120, 60));
        SwingUtilities.invokeLater(() -> {
            Submission submission = CVManager.uploadCV(selectedFile, user, title, desc, jobTitle);
            if (submission != null) {
                feedbackLabel.setText("CV uploaded successfully!");
                feedbackLabel.setForeground(new Color(0, 153, 51));
                fileField.setText("");
                titleField.setText("");
                descriptionArea.setText("");
                selectedFile = null;
                uploadButton.setEnabled(false);
                // Show match result dialog
                java.util.Map<String, Object> result = com.cvreviewapp.utils.CVManager.checkCV(submission.getFilePath(), jobTitle);
                StringBuilder msg = new StringBuilder();
                if (result.containsKey("error")) {
                    msg.append(result.get("error"));
                } else {
                    msg.append("Job Title: ").append(result.get("jobTitle")).append("\n");
                    msg.append("\nMissing Skills: ").append(result.get("missingSkills")).append("\n");
                    msg.append("Qualification Present: ").append(result.get("hasQualification")).append("\n");
                    msg.append("Experience Present: ").append(result.get("hasExperience")).append("\n");
                }
                javax.swing.JButton backBtn = new javax.swing.JButton("Back to Home");
                backBtn.addActionListener(ev -> {
                    new HomePage();
                    javax.swing.SwingUtilities.getWindowAncestor(CVUploadPanel.this).dispose();
                });
                javax.swing.JPanel panel = new javax.swing.JPanel();
                panel.setLayout(new java.awt.BorderLayout());
                panel.add(new javax.swing.JScrollPane(new javax.swing.JTextArea(msg.toString())), java.awt.BorderLayout.CENTER);
                panel.add(backBtn, java.awt.BorderLayout.SOUTH);
                javax.swing.JOptionPane.showMessageDialog(CVUploadPanel.this, panel, "CV Match Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                feedbackLabel.setText("Upload failed. Please try again.");
                feedbackLabel.setForeground(Color.RED);
            }
        });
    }
} 