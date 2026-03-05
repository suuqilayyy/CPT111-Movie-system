package movietracker.backend;

import java.util.ArrayList;

/**
 * Utility class for parsing CSV lines with proper handling of quotes and commas.
 */
public class CsvParser {
    
    /**
     * Parses a CSV line, handling commas within quoted fields.
     * @param line The CSV line to parse
     * @return Array of parsed values
     */
    public static String[] parseCsvLine(String line) {
        ArrayList<String> tokens = new ArrayList<>();
        String current = "";
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // If we're inside quotes and see a doubled quote, treat it as a literal quote
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current = current + '"';
                    i++; // skip the next quote
                    continue;
                }
                inQuotes = !inQuotes; // Toggle quote status
                continue; // Skip the quote character itself
            }

            if (c == ',' && !inQuotes) {
                // Found delimiter outside quotes, save current token
                tokens.add(current.trim());
                current = ""; // Reset for next token
            } else {
                current = current + c;
            }
        }
        tokens.add(current.trim());
        return tokens.toArray(new String[0]);
    }
    
    /**
     * Checks if a line is a CSV header by looking for common header patterns.
     * @param line The line to check
     * @param expectedPrefix The expected prefix for a header line
     * @return True if the line is a header, false otherwise
     */
    public static boolean isHeaderLine(String line, String expectedPrefix) {
        return line != null && line.toLowerCase().startsWith(expectedPrefix.toLowerCase());
    }
    
    /**
     * Checks if a line is empty or contains only whitespace.
     * @param line The line to check
     * @return True if the line is empty, false otherwise
     */
    public static boolean isEmptyLine(String line) {
        return line == null || line.trim().isEmpty();
    }
}
