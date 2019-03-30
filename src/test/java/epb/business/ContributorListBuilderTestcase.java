package epb.business;

import epb.utils.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

/**
 * Tests {@link ContributorListBuilder}
 *
 */
public class ContributorListBuilderTestcase {

    private static final String TEST_RESOURCES = "target/test-classes/business/";

    private static File inputFile = new File(TEST_RESOURCES, "contributors.properties");

    private static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of("epub 2", new ContributorListBuilder(inputFile, 2), new File(TEST_RESOURCES, "expected_epub2.txt")),
                Arguments.of("epub 3", new ContributorListBuilder(inputFile, 3), new File(TEST_RESOURCES, "expected_epub3.txt"))
        );
    }

    @ParameterizedTest(name = "test contributor list for {0}")
    @MethodSource("arguments")
    public void testContributorListEpub(String name, ContributorListBuilder builder, File expectedResultFile) throws Exception {
        final String content = builder.build();
        final String expected = IOUtils.read(expectedResultFile);
        Assertions.assertEquals(expected.trim(), content.trim());
    }

}
