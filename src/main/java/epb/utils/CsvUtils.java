package epb.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface CsvUtils {

    /**
     * Read a CSV File into a list of string array
     *
     * @return the csv content
     */
    static List<String[]> readCSV(File csvFile) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(csvFile))) {
            List<String[]> list = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue; // ignoring empty line
                }
                if (line.endsWith(";")) {
                    line += " ";// adding an extra space so split works correctly
                }
                list.add(line.split(";"));
            }
            return list;
        }
    }

    /**
     * Sort the input list based on the first element of each array (must be integer)
     *
     * @param data the list of string arrays to sort
     */
    static void sortCSVData(List<String[]> data) {
        data.sort((o1, o2) -> {
            // TODO use an exception instead of assertion as it is no longer private method
            assert o1.length > 1 && o2.length > 1 : "CSV data invalid";
            final Integer playorder1 = Integer.parseInt(o1[0]);
            final Integer playorder2 = Integer.parseInt(o2[0]);
            return playorder1.compareTo(playorder2);
        });
    }

}
