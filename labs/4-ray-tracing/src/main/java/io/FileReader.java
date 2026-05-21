package io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class FileReader {
    private final BufferedReader reader;

    public FileReader(String path) throws FileNotFoundException {
        reader = new BufferedReader(new java.io.FileReader(path));
    }

    public String getMeaningfulLine() throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            int commentIndex = line.indexOf("//");

            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }

            line = line.strip();

            if (!line.isEmpty()) {
                return line;
            }
        }

        throw new IOException("Reached end of file");
    }

    public String[] getLineTokens() throws IOException {
        return getMeaningfulLine().split("\\s+");
    }

    public int[] getLineNumbers() throws IOException {
        return Arrays.stream(getLineTokens())
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    public double[] getLineDoubles() throws IOException {
        return Arrays.stream(getLineTokens())
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    public void close() throws IOException {
        reader.close();
    }
}