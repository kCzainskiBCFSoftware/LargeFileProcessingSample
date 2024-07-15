package com.daycode;

import org.junit.jupiter.api.Disabled;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Rough test file generation only for testing purposes.
 */
@Disabled
public class GenerateDataTest {

    public static void main(String[] args) {
        String inputFile = "src/test/resources/example_file.csv";
        String outputFile = "src/main/resources/large_file.csv";
        long targetSizeInBytes = 5L * 1024 * 1024 * 1024; // 5 GB
        long printIntervalInBytes = 100L * 1024 * 1024; // 100 MB

        try {
            // Read the input file
            byte[] fileBytes = Files.readAllBytes(Paths.get(inputFile));
            String fileContent = new String(fileBytes);

            // Calculate the number of lines required to reach the target size
            long currentSize = fileBytes.length;
            long numberOfLines = Files.lines(Paths.get(inputFile)).count();
            long averageLineSize = currentSize / numberOfLines;
            long linesRequired = (targetSizeInBytes - currentSize) / averageLineSize;

            // Prepare the writer for the output file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(fileContent); // Write the existing content
            if (!fileContent.endsWith("\n")) {
                writer.write("\n");
            }

            // Random data generation setup
            Random random = new Random();
            String[] cities = {"Warszawa", "Wrocław", "Kraków", "Poznań", "Zielona Góra", "Opole", "Berlin"};
            long initialTimestamp = 1537315052619L; // Example timestamp
            double minTemp = -20.0;
            double maxTemp = 40.0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


            // Generate additional records
            long generatedBytes = currentSize;
            for (long i = 0; i < linesRequired; i++) {
                long timestamp = initialTimestamp + i * 10000;// Increment timestamp by 10 seconds
                Date date = new Date(timestamp);
                String formattedDate = dateFormat.format(date);
                double temperature = minTemp + (maxTemp - minTemp) * random.nextDouble();
                String city = cities[random.nextInt(cities.length)];
                String newLine = String.format("%s;%s;%.2f%n", city, formattedDate, temperature);
                writer.write(newLine);
                generatedBytes += newLine.getBytes().length;

                // Print progress every 100MB
                if (generatedBytes >= printIntervalInBytes) {
                    System.out.println("Generated: " + (generatedBytes / (1024 * 1024)) + " MB");
                    printIntervalInBytes += 100L * 1024 * 1024; // Update the next print interval
                }
            }

            // Close the writer
            writer.close();
            System.out.println("File generation completed.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
