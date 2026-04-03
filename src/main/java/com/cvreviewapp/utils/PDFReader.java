package com.cvreviewapp.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Enhanced PDF text extraction utility with robust exception handling and resource management.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class PDFReader {
    private static final Logger LOGGER = Logger.getLogger(PDFReader.class.getName());

    private PDFReader() {}

    /**
     * Extracts text from a PDF file using PDFBox.
     * 
     * @param file The PDF file
     * @return Extracted text or null if failed
     */
    public static String extractText(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            LOGGER.warning("Invalid file provided for PDF extraction: " + file);
            return null;
        }

        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            LOGGER.log(Level.INFO, "Successfully extracted text from: {0}", file.getName());
            return text;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to extract text from PDF: " + file.getName(), e);
        }
        return null;
    }
}