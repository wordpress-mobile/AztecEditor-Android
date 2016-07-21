package org.wordpress.aztec

/**
 * Test utilities to be used with unit tests.
 */
object TestUtils {
    /**
     * Compare two strings ignoring case and whitespace.  Two strings are considered equal if they
     * are of the same length and corresponding characters while ignoring case and whitespace.
     *
     * @param first The first string to compare.
     * @param second The second string to compare.
     * @return True if first equals second ignoring case and whitespace.  False otherwise.
     */
    fun equalsIgnoreCaseAndWhitespace(first: String, second: String): Boolean {
        return first.replace("\\s+".toRegex(), "").equals(second.replace("\\s+".toRegex(), ""), ignoreCase = true)
    }

    /**
     * Compare two string ignoring whitespace.  Two strings are considered equal if they are of the
     * same length and corresponding characters while ignoring whitespace.
     *
     * @param first The first string to compare.
     * @param second The second string to compare.
     * @return True if first equals second ignoring whitespace.  False otherwise.
     */
    fun equalsIgnoreWhitespace(first: String, second: String): Boolean {
        return first.replace("\\s+".toRegex(), "") == second.replace("\\s+".toRegex(), "")
    }
}
