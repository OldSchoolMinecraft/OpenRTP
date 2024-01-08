package com.oldschoolminecraft.openrtp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {
    public static void writeResultsToCSV(List<String[]> results, String[] columnNames, String filePath)
    {
        try (FileWriter writer = new FileWriter(filePath))
        {
            writeRow(writer, columnNames); // Write the header row

            for (String[] row : results)
                writeRow(writer, row);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[OpenRTP] Error writing CSV file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[OpenRTP] Unexpected error: " + e.getMessage());
        }
    }

    private static void writeRow(FileWriter writer, String[] row) throws IOException
    {
        for (int i = 0; i < row.length; i++)
        {
            String data = row[i] != null ? row[i] : "N/A"; // Replace missing values with "N/A"
            writer.write(escapeSpecialCharacters(data));

            if (i != row.length - 1)
                writer.write(",");
        }
        writer.write("\n");
    }

    private static String escapeSpecialCharacters(String data)
    {
        // If the data contains special characters (e.g., commas, quotes),
        // enclose it in double quotes and escape any existing double quotes.
        if (data.contains(",") || data.contains("\""))
            data = "\"" + data.replace("\"", "\"\"") + "\"";
        return data;
    }
}
