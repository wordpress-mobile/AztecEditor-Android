package org.wordpress.aztec.plugins.shortcodes.utils

class GutenbergUtils {
    companion object {
        val GUTENBERG_BLOCK_START = "<!-- wp:"
        /*
            Note the way we detect we're in presence of Gutenberg blocks logic is taken from
            https://github.com/WordPress/gutenberg/blob/5a6693589285363341bebad15bd56d9371cf8ecc/lib/register.php#L331-L345

            * Determine whether a content string contains blocks. This test optimizes for
            * performance rather than strict accuracy, detecting the pattern of a block
            * but not validating its structure. For strict accuracy, you should use the
            * block parser on post content.
            *
            * @since 1.6.0
            * @see gutenberg_parse_blocks()
            *
            * @param string $content Content to test.
            * @return bool Whether the content contains blocks.

            function gutenberg_content_has_blocks( $content ) {
                return false !== strpos( $content, '<!-- wp:' );
            }
         */
        fun contentContainsGutenbergBlocks(content: String?): Boolean {
            return content != null && content.contains(GUTENBERG_BLOCK_START)
        }
    }
}
