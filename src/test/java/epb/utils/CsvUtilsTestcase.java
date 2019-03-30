package epb.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test case for {@link CsvUtils}
 */
public class CsvUtilsTestcase {

    private static final String CSV_FILE = "target/test-classes/utils/example.csv";

    private static final List<String[]> EXPECTED_CSV = Arrays.asList(
            new String[]{"1","cell11","cell12","cell13"},
            new String[]{"2","cell21","cell22","cell23"},
            new String[]{"3","cell31","cell32","cell33"},
            new String[]{"4","cell41","cell42","cell43"}
    );

    private static final List<String[]> RANDOM_CSV = Arrays.asList(
            new String[]{"3","cell31","cell32","cell33"},
            new String[]{"4","cell41","cell42","cell43"},
            new String[]{"2","cell21","cell22","cell23"},
            new String[]{"1","cell11","cell12","cell13"}
    );

    /**
     * tests {@link CsvUtils#readCSV(File)} }
     */
    @Test
    public void testReadCsv() throws Exception {
        List<String[]> parsedCsv = CsvUtils.readCSV(new File(CSV_FILE));
        assertEquals(EXPECTED_CSV, parsedCsv);
    }

    /**
     * tests {@link CsvUtils#sortCSVData(List)} }
     */
    @Test
    public void testSortCsv() {
        CsvUtils.sortCSVData(RANDOM_CSV);
        assertEquals(EXPECTED_CSV, RANDOM_CSV);
    }

    private void assertEquals(List<String[]> expected, List<String[]> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        for(int i = 0 ; i < expected.size() ; i++){
            Assertions.assertArrayEquals(expected.get(i), actual.get(i));
        }
    }

}
