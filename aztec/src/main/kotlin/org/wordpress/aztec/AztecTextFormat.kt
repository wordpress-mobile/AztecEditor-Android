package org.wordpress.aztec

/**
 * Describes styles that could be applied to text
 */
enum class AztecTextFormat : ITextFormat {
    FORMAT_NONE,
    FORMAT_HEADING_1,
    FORMAT_HEADING_2,
    FORMAT_HEADING_3,
    FORMAT_HEADING_4,
    FORMAT_HEADING_5,
    FORMAT_HEADING_6,
    FORMAT_UNORDERED_LIST,
    FORMAT_ORDERED_LIST,
    FORMAT_TASK_LIST,
    FORMAT_BOLD,
    FORMAT_STRONG,
    FORMAT_ITALIC,
    FORMAT_EMPHASIS,
    FORMAT_CITE,
    FORMAT_UNDERLINE,
    FORMAT_STRIKETHROUGH,
    FORMAT_ALIGN_LEFT,
    FORMAT_ALIGN_CENTER,
    FORMAT_ALIGN_RIGHT,
    FORMAT_QUOTE,
    FORMAT_LINK,
    FORMAT_HORIZONTAL_RULE,
    FORMAT_PARAGRAPH,
    FORMAT_PREFORMAT,
    FORMAT_BIG,
    FORMAT_SMALL,
    FORMAT_SUPERSCRIPT,
    FORMAT_SUBSCRIPT,
    FORMAT_FONT,
    FORMAT_MONOSPACE,
    FORMAT_CODE,
    FORMAT_MARK,
    FORMAT_HIGHLIGHT,

    // note(alex): Storypark added default text formats. Now we could get this behavior through
    //             the plugins API. However, a lot of the default formatters/watchers require
    //             iterating through this enum. Making plugins, mostly in-extensible unless you
    //             have a specific use case like the upstream comments plugin
    FORMAT_COLOR, // Foreground text color
    FORMAT_TYPEFACE, // Typeface based span instead of fontFamily based default
    FORMAT_ABSOLUTE_FONT_SIZE, // Absolute size span
    FORMAT_ABSOLUTE_LINE_HEIGHT // A metric affecting span adjusting line height, inline
}
