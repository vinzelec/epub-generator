package epb;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;

/**
 * test case for the {@link ReplaceTask}
 */
public class ReplaceTaskTestcase {

    private static final String TEST_RESOURCES = "target/test-classes/";

    private ReplaceTask testedTask = new ReplaceTask();

    // TODO add more test cases
    @ParameterizedTest(name="test replace {0} with {1}")
    @CsvSource({
            TEST_RESOURCES+"inputFile1.txt, "+TEST_RESOURCES+"replace.properties, "+TEST_RESOURCES+"ok1.txt",
            TEST_RESOURCES+"inputFile2.txt, "+TEST_RESOURCES+"replace.properties, "+TEST_RESOURCES+"ok2.txt",
            TEST_RESOURCES+"inputFile3.txt, "+TEST_RESOURCES+"latin.properties, "+TEST_RESOURCES+"ok3.txt"
    })
    public void testReplace(String file, String properties, String expected) throws Exception {
        testedTask.setFile(file);
        testedTask.setProperties(properties);
        testedTask.execute();

        String result = IOUtils.read(new File(file));
        String expectedContent = IOUtils.read(new File(expected));
        assertEquals(expectedContent, result);
    }

}
