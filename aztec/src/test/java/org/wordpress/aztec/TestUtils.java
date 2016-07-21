package org.wordpress.aztec;

/**
 * Test utilities to be used with unit tests.
 */
public class TestUtils {
    private TestUtils() {
    }

    /**
     * Compare two strings ignoring case and whitespace.  Two strings are considered equal if they
     * are of the same length and corresponding characters while ignoring case and whitespace.
     *
     * @param first The first string to compare.
     * @param second The second string to compare.
     * @return True if first equals second ignoring case and whitespace.  False otherwise.
     */
    public static boolean equalsIgnoreCaseAndWhitespace(String first, String second) {
        return first.replaceAll("\\s+", "").equalsIgnoreCase(second.replaceAll("\\s+", ""));
    }

    /**
     * Compare two string ignoring whitespace.  Two strings are considered equal if they are of the
     * same length and corresponding characters while ignoring whitespace.
     *
     * @param first The first string to compare.
     * @param second The second string to compare.
     * @return True if first equals second ignoring whitespace.  False otherwise.
     */
    public static boolean equalsIgnoreWhitespace(String first, String second) {
        return first.replaceAll("\\s+", "").equals(second.replaceAll("\\s+", ""));
    }
}
