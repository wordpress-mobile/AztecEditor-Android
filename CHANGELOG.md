# Changelog

## [1.3.44](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.44)
### Changed
- Updated strikethrough to default to `<s>` tag (#919)

## [1.3.43](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.43)
### Changed
- Fixed the add image icon in Aztec Toolbar (#908)

## [1.3.42](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.42)
### Changed
- Update block-based span classes to allow view level alignment rendering (#899)

## [1.3.41](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.41)
### Changed
- Add option to disable collapsing of whitespaces

## [1.3.40](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.40)
### Changed
- Adds support to to customise styles for the Aztec Toolbar

## [1.3.39](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.39)
### Fixed
- Move media button initialization to avoid crash when advanced toolbar is enabled and media button is disabled

## [1.3.38](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.38)
### Fixed
- When text changes, only disable selection listener if text is empty

## [1.3.37](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.37)
### Fixed
- Resolved clipboard issue on Android 7
### Changed
- Improved TalkBack handling of formatting toolbar
- Added ability to register an observer for all the changes to the AztecText object

## [1.3.36](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.36)
### Changed
- Implement start and reversed in lists
- Allow preformatted background alpha and tidying to be set from child classes

## [1.3.35](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.35)
### Changed
- Update Glide version to 4.10.0

## [1.3.34](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.34)
### Added
- Added the ability to report to the host app the IndexOutOfBounds exception
 in android.text.DynamicLayout.getBlockIndex(DynamicLayout.java:646) under Android 8.XX (#861)

## [1.3.33](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.33)
### Fixed
- IndexOutOfBounds case in DynamicLayout.reflow() under Android 8.0.0 (#834)

## [1.3.32](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.32)
### Fixed
- Allow paragraph spans that do not extend AlignmentSpan (#851)

## [1.3.31](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.31)
### Fixed
- Fix lag deleting and inserting text #853 and #854

## [1.3.30](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.30)
### Added
- Added the ability to signal undo/redo events to host app

## [1.3.29](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.29)
### Fixed
- Fix span overlapping on window resizing (Chromebooks)
- Fix cursor size on new paragraph line (Chromebooks)
- Fix breaking layout on slow window resizing (Chromebooks)

## [1.3.28](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.28)
### Added
- Added support for AndroidX

## [v1.3.27](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.27)
### Fixed
- Gutenberg fixes:
  * Suppress multiple onSelectionChanged events when listener is disabled #817
  * Avoid trimming spaces around html block elemts


## [v1.3.26](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.26)
### Fixed
- DynamicLayout Crash #813
- List handling in Gutenberg #812


## [v1.3.25](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.25)
### Changed
- Remove some unneeded logging #806
### Fixed
- Don't bail when ZWJ cleanup found end-of-text marker #807
- Prevent execution of InputFilter for new lines #809

## [v1.3.24](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.24)
### Changed
- Improvement to how clickable URL spans are handled #793

## [v1.3.23](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.23)
### Fixed
- Fix for crash when inserting characters right before an image #801

## [v1.3.22](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.22)
### Fixed
- Fix for crash on Samsung Clipboard ListView on Android 8.X #795

## [v1.3.20](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.20)
### Changed
- Support for `<em>` for italic and using it as default via the formatting toolbar #777

## [v1.3.19](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.19)
### Fixed
- Option to avoid autofocus when getting visible #783

## [v1.3.18](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.18)
### Fixed
- Revert merge #776

## [v1.3.17](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.17)
### Fixed
- Add jitpack.yml to work around the Google licence issue (#778)

## [v1.3.16](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.16)
### Fixed
- Fix onSelectionChange being emitted twice when calling fromHTML (#776)

## [v1.3.15](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.15)
### Changed
- Synced the caption span alignment attribute with its the align property

## [1.3.14](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.14) - 2019-01-11
### Fixed
- Fixed issue where pasting over the whole text emitted a delete char, causing Gutenberg to remove whole block
- Updated Kotlin version to use stable version of Coroutines

## [1.3.13](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.13) - 2018-12-20
### Fixed
- Fixed issue where `onEnterKey` key listener was called with the wrong text.

## [1.3.12](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.12) - 2018-11-19
### Fixed
- Fixed: Repeating characters in the editor (fixed previous release)

## [1.3.11](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.11) - 2018-11-08
### Fixed
- Fixed: Repeating characters in the editor

## [1.3.10](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.10) - 2018-10-23
### Fixed
- Fixed: crash when deleting text (span handling)

## [1.3.9](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.9) - 2018-09-06
### Added
- Added support for creating links that open in a new window.

### Fixed
- Fixed: don't perform initial hash calculation when pasting content

## [1.3.8](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.8) - 2018-08-28
### Added
- Added the ability to use the new Android 8 feature "paste as plain text"

## [1.3.7](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.7) - 2018-06-29
### Fixed
- Fixed initial hash calculation for change detection mechanism.
- Fixed crash with too many nested `<b>` tags.

### Changed
- Support for the `div`, `figure`, `figcaption` and `section` html elements to be block-level compatible instead of in-line.

## [1.3.6](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.6) - 2018-06-18
### Fixed
- Fixed handling of schemed urls (mailto:, http, #, etc.).
- Fixes a break in http:// descriptor as per a CSS readability feature would add a space after ":".
- Crash fixes.

### Changed
- Cursor position is now retained when switching from Visual to HTML mode.
- Change detection is now handled through a hash comparison implementation and syncs between HTML/visual editors.
- The `<strong>` tag is now the styling element used when tapping on the B toolbar button.

## [1.3.5](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.5) - 2018-06-04
### Fixed
- `<cite>` tags are now respected (were being converted to `<i>`).
- Fixed a bug where nested paragraphs would get duplicated
- Fixed Gutenberg audio and video blocks handling

## [1.3.4](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.4) - 2018-05-21
### Fixed
- Better Gutenberg posts compatibility support (Gutenberg block delimiters are not lost anymore)
- Removed duplicate shortcut for Chromebook.

## [1.3.3](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.3) - 2018-05-02
### Fixed
- Fix an issue that removed attributes from HR tags.
- Fix a focusing issue when copying text in an AztecText.

### Changed
- Hide the HTML button if the source editor is not set.

## [1.3.2](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.2) - 2018-04-12
### Fixed
- Fix a crash with the "Paste As Plain Text" feature from Android O.

### Changed
- Accessibility: Add content description for heading and list menu items.
- Accessibility: Add support for touch exploration.

## [1.3.1](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3.1) - 2018-03-15
### Fixed
- Fix a crash with big post content (+400Kb)

## [1.3](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.3) - 2018-03-02
### Added
- RTL layout support for style toolbar and undo/redo action bar
- RTL text support for quote and list rendering
- Ability to apply block formatting across multiple nesting levels of elements

### Fixed
- Block element closing with a newline in nested elements
- List formatting when applied to the last line
- Applying block formatting over list items
- Line indicator rendering for items containing other block elements

### Changed
- Quote toggling, which removes the entire block now instead of a single line

## [1.2](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.2) - 2018-02-09
### Added
- Text & image caption alignment support

### Fixed
- Crash when editing a heading
- Order of inserted images

## [1.1](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.1) - 2018-01-29
### Added
- Plugin that detects and hides Gutenberg editor blocks in the visual editor
- Plugin that adds support for CSS-style underline formatting

### Fixed
- Bug on API 26+ when onMediaDeleted called even when no image was deleted
- Slow edit history performance
- Lost formatting on API 25 when inserting space before styled text
- Lost formatting on API 26+ when inserting a newline before styled text

## [1.0.1](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0.1) - 2018-01-22
### Fixed
- Crash when pasting certain text

## [1.0](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0) - 2018-01-11
### Fixed
- Various problems when editing captions
- Crash when copy/pasting certain text
- Crash when applying <hr>
- Cursor position after adding special comments or <hr>
- Image/video placeholder dimensions
- Problem when tapping an unknown element placeholder did not show the editor
- Text styling error when inserting a space before a formatted text

### Added
- VideoPress shortcode support
- Support for style attribute interpretation of CSS (text color)
- Ability to hide HTML comments in visual mode
- New media toolbar menu

### Changed
- Automatic caption deletion when it's empty
- Deleting an image now removes the caption

## [1.0-beta.12](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.12) - 2017-12-06
### Fixed
- Text handling following immediately after images
- Undo/redo edit history
- Demo app sample text
- URL span not including href attribute when other attributes are present
- Broken parsing of <audio> and <video> tags
- Broken UI tests
- All content appearing in one line on older API levels
- Fixed image scaling
- RTL support for lists
- Text editing around images with captions
- Images not staying inside links

### Added
- Check for correct paragraph bounds to prevent crashes + logging if it's incorrect
- New link UI tests

## [1.0-beta.11](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.11) - 2017-11-20
### Fixed
- Paragraphs with attributes being removed
- Conflicts of autocorrect/suggestions with editor styling
- Numbered list item display issues with multiple digits
- Abnormally tall lines following images without line breaks
- Incorrect click-handling around images not on separate lines

### Changed
- All images are made to be full-width & on separate lines

### Added
- Toolbar state notification interface for plugins
- Ability to place cursor next to an image
- Spellcheck suggestions
- Improved image-related performance

## [1.0-beta.10](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.10) - 2017-10-06
### Fixed
- crash when HTML tags contained special regex characters ('$' or '\\')

## [1.0-beta.9](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.9) - 2017-09-25
### Fixed
- OOB crash when unmarshalling URL span

## [1.0-beta.8](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.8) - 2017-08-28
### Changed
- Gradle version to 4.1 and use the new `implementation` configuration to stop transient dependency leaking

### Added
- Configuration option to use the visual editor without the HTML editor

## [1.0-beta.7](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.7) - 2017-08-15
### Changed
- Toolbar button highlighted state to use a color with more contrast

### Fixed
- Image/video loading placeholder drawable usage
- Quote styling of the paragraph ends, empty and mixed lines
- The missing hint bug if text is empty
- Crash in the URL dialog caused by the non plain text content of a clipboard
- Crash when copy/pasting lists
- Copy/pasting of non-latin unicode characters

### Added
- This changelog
- Plugin interface for regex plain text handler
- Plugin interface for HTML preprocessor
- Plugin interface for HTML postprocessor
- Plugin module with `[video]`, `[audio]` and `[caption]` WordPress shrotcode support
- OnMediaDeletedListener interface, detection and handling
- Copy/pasting of block element styles
- Copy/pasting of styled text and HTML from external sources

## [1.0-beta.6](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.6) - 2017-07-25
### Changed
- Plugin interface refactoring
- Disabled the memory optimization of drawables (temporary fix for image disappearing bug)

### Fixed
- Fixed image tap detection

## [1.0-beta.5](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.5) - 2017-07-21
### Fixed
- A possible memory leak connected to drawables
- OOB crash in AztecMediaSpan

### Changed
- Disabled styling of multiline text with lingering newlines (temporary crash fix)

## [1.0-beta.4](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.4) - 2017-07-14
### Added
- 1st stage of plugin architecture redesign – moved special comments to a separate WordPress comments plugin

### Fixed
- Some UI tests
- A crash during quote parsing
- List styling, when 1st line was empty
- A bug when toolbar buttons only work when device keyboard is open
- A bug when style toolbar was erroneously highlighted

## [1.0-beta.3](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.3) - 2017-07-03
### Fixed
- Toolbar ellipsis button disabled state
- Toolbar collapse/expand triggers
- Jittery cursor movement during media upload

### Added
- Added button click callbacks

### Changed
- Updated toolbar layout to allow switching between simple/advanced mode

## [1.0-beta.2](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.2) - 2017-06-20
### Added
- Implemented <video> tag support & video playback
- Added UI tests for text formatting

### Changed
- Major toolbar redesign & improvements (button reordering, updated icons & colors, new advanced mode, new list & heading menus and more)
- Updated example content in the demo app

### Fixed
- A bug that prevented an inline style to be removed in the middle of text
- A link selection crash
- A bug that caused a wrong block element being selected in the toolbar

## [1.0-beta.1](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta.1) - 2017-06-05
### Added
- `<pre>` tag with white-space formatting support

### Fixed
- Changing heading type for multiple headings
- Disappearing context menu during image upload
- Paragraphs disappearing while deleting

### Changed
- Toolbar buttons selected state style

## [1.0.0-beta](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v1.0-beta) - 2017-04-24
### Added
- Paragraph support for double-newline separation in HTML
- Paragraph (larger) break on return key
- Support for embedded lists
- Ability to update element attributes from the library client

### Fixed
- The heading menu bug
- The crash when pressing undo while uploading an image

## [0.5.0-alpha.5](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v0.5.0-alpha.5) - 2017-04-11
### Added
- Support for the `<hr>` tag

### Fixed
- A bug that resulted in quote style removal
- Another quote style rendering issue
- The crash when adding an image to an empty post
- The freezing & lagging when having multiple images in a post
- The weird cursor behavior around special comments

## [0.5.0-alpha.4](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v0.5.0-alpha.4) - 2017-03-27
### Fixed
- Toolbar crash
- The heading-deletion crash
- A strange behavior when adding multiple different heading
- Nested lists getting removed when switching to HTML mode
- Disappearing newlines between headings
- Text inside paragraph getting moved outside when toggling HTML mode
- Heading inside list & quote adding extra newlines
- Using backspace on Nougat causing weird cursor behavior
- Weird cursor behavior when deleting text inside quote
- Heading inside lists adding extra newlines
- Multiple bullets with nested lists
- The nesting of lists/quotes getting swapped when toggling HTML mode
- Closing lists with double enter adding extra newline
- Double space to end sentence inserting a space before period

### Changed
- Updated the Heading icon
- Updated unknown-HTML dialog style to match the WordPress colors

### Removed
- Dropped the Merriweather font & replaced it with default serif & monospace fonts

## [0.5.0-alpha.3](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v0.5.0-alpha.3) - 2017-02-27
### Fixed
- Cursor visibility on highlighted background
- Broken links on older Android versions
- Empty block quotes being removed
- The link crash

### Changed
- Wide images inside lists are now adjusted to fit
- Span tag is now formatted inline

### Added
- Toolbar state is preserved on rotation
- Keyboard shortcuts for format toolbar buttons
- New unknown HTML (question mark icon) edit dialog

## [0.5.0-alpha.2](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v0.5.0-alpha.2) - 2017-02-13
### Fixed
- a handful of bugfixes

## [0.5.0-alpha](https://github.com/wordpress-mobile/AztecEditor-Android/releases/tag/v0.5.0-alpha) - 2017-02-08
### Added
- Synchronized visual and HTML mode
- Edit history with undo/redo
- Images (both downloading & adding)
- Headings
- Text styling (bold, italic, underline, strike-through)
- Quotes
- Lists (numbered & bullets)
- Links
- HTML comments
- More & page special tags
- Hidden tags (`div`, `span`)
- Unknown HTML (replaced with a question mark in visual mode)
- Emojis
