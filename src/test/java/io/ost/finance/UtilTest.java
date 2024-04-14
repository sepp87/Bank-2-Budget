package io.ost.finance;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joost
 */
public class UtilTest {

    @org.junit.jupiter.api.BeforeAll
    @org.junit.jupiter.api.AfterAll
    public static void printLine() {
        System.out.println();
    }

    public static void printResult(Object expected, Object result) {
        System.out.println("Expected:\t" + Util.padWithTabs(expected, 4) + "\tResult: " +  Util.padWithTabs(result, 2));
    }

    @org.junit.jupiter.api.Test
    public void testFindOverlap_WhenTimespanOverlapsWithOtherTimespan_ThenReturnOverlap() {
        System.out.println("testFindOverlap_WhenTimespanOverlapsWithOtherTimespan_ThenReturnOverlap");

        // Create test data
        List<LocalDate> list1 = getList("2024-01-01", "2024-01-10");
        List<LocalDate> list2 = getList("2024-01-09", "2024-01-20");

        // Perform test
        LocalDate[] result = Util.findOverlap(list1, list2);

        // Prepare results
        LocalDate[] expected = {LocalDate.parse("2024-01-09"), LocalDate.parse("2024-01-10")};

        // Evaluate result
        assertArrayEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    @org.junit.jupiter.api.Test
    public void testFindOverlap_WhenTimespanIsContainedWithinOtherTimespan_ThenReturnOverlap() {
        System.out.println("testFindOverlap_WhenTimespanIsContainedWithinOtherTimespan_ThenReturnOverlap");

        // Create test data
        List<LocalDate> list1 = getList("2024-01-01", "2024-01-10");
        List<LocalDate> list2 = getList("2024-01-03", "2024-01-05");

        // Perform test
        LocalDate[] result = Util.findOverlap(list1, list2);

        // Prepare results
        LocalDate[] expected = {LocalDate.parse("2024-01-03"), LocalDate.parse("2024-01-05")};

        // Evaluate result
        assertArrayEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    @org.junit.jupiter.api.Test
    public void testFindOverlap_WhenTimespansDoNotOverlap_ThenReturnNull() {
        System.out.println("testFindOverlap_WhenTimespansDoNotOverlap_ThenReturnNull");

        // Create test data
        List<LocalDate> list1 = getList("2024-01-01", "2024-01-10");
        List<LocalDate> list2 = getList("2024-01-11", "2024-01-20");

        // Perform test
        LocalDate[] result = Util.findOverlap(list1, list2);

        // Prepare results
        LocalDate[] expected = null;

        // Evaluate result
        assertArrayEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    private List<LocalDate> getList(String from, String to) {
        List<LocalDate> result = new ArrayList<>();
        result.add(LocalDate.parse(from));
        result.add(LocalDate.parse(to));
        return result;

    }
}
