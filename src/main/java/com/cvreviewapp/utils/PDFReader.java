package com.cvreviewapp.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class PDFReader {
    public static boolean isValidPDF(File file) {
        try (PDDocument doc = PDDocument.load(file)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractText(File file) {
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        } catch (Exception e) {
            System.err.println("[PDFReader] Failed to extract text: " + e.getMessage());
            return "";
        }
    }
} 