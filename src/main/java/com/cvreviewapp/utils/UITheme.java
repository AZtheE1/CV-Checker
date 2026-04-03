package com.cvreviewapp.utils;

import java.awt.Color;
import java.awt.Font;
import javax.swing.border.EmptyBorder;

/**
 * Centered UI Theme constants for maintaining consistent design across the application.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public final class UITheme {
    private UITheme() {}

    // Colors
    public static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    public static final Color SECONDARY_COLOR = new Color(25, 118, 210);
    public static final Color ACCENT_COLOR = new Color(255, 64, 129);
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color TEXT_COLOR = new Color(33, 33, 33);
    public static final Color ERROR_COLOR = new Color(211, 47, 47);
    public static final Color SUCCESS_COLOR = new Color(56, 142, 60);

    // Fonts
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font REGULAR_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);

    // Padding & Layout
    public static final EmptyBorder MAIN_PADDING = new EmptyBorder(20, 20, 20, 20);
    public static final int COMPONENT_SPACING = 10;
    public static final int BORDER_RADIUS = 8;

    public static void setupGlobalStyles() {
        // Global Swing UI configuration can be added here
    }
}
