# Changelog

All notable changes to this project will be documented in this file.

## [1.0-beta.6] - 2017-07-25
- Plugin interface refactoring
- Fixed image tap detection
- Disabled the memory optimization of drawables (temporary fix for image disappearing bug)

## [1.0-beta.5] - 2017-07-21
- Fixed a possible memory leak connected to drawables
- Fixed OOB crash in AztecMediaSpan
- Disabled styling of multiline text with lingering newlines (temporary crash fix)

## [1.0-beta.4] - 2017-07-14
- 1st stage of plugin architecture redesign – moved special comments to a separate WordPress comments plugin
- Some UI tests got fixed
- Fixed a crash during quote parsing
- Fixed list styling, when 1st line was empty
- Fixed a bug when toolbar buttons only work when device keyboard is open
- Fixed a bug when style toolbar was erroneously highlighted

## [1.0-beta.3] - 2017-07-03
- Fixed toolbar ellipsis button disabled state
- Added button click callbacks
- Updated toolbar layout to allow switching between simple/advanced mode
- Fixed toolbar collapse/expand triggers
- Fixed jittery cursor movement during media upload

## [1.0-beta.2] - 2017-06-20
- Major toolbar redesign & improvements (button reordering, updated icons & colors, new advanced mode, new list & heading menus and more)
- Fixed a bug that prevented an inline style to be removed in the middle of text
- Implemented <video> tag support & video playback
- Fixed a link selection crash
- Updated example content in the demo app
- Added UI tests for text formatting
- Fixed a bug that caused a wrong block element being selected in the toolbar

## [1.0-beta.1] - 2017-06-05
- `<pre>` tag with white-space formatting support
- Changing heading type for multiple headings bug fix
- Disappearing context menu during image upload fix
- Paragraphs disappearing while deleting fix
- Toolbar buttons selected state style change

## [1.0.0-beta] - 2017-04-24
- Fixed the heading menu bug
- Implemented paragraph support for double-newline separation in HTML
- Implemented paragraph (larger) break on return key
- Added support for embedded lists
- Fixed the crash when pressing undo while uploading an image
- Added ability to update element attributes from the library client

## [0.5.0-alpha.5] - 2017-04-11
- Added support for the `<hr>` tag
- Fixed a bug that resulted in quote style removal
- Fixed another quote style rendering issue
- Fixed the crash when adding an image to an empty post
- Fixed the freezing & lagging when having multiple images in a post
- Fixed the weird cursor behavior around special comments

## [0.5.0-alpha.4] - 2017-03-27
- Fixed Toolbar crash report
- Having multiple bullets with nested lists
- Nested lists getting removed when switching to HTML mode
- The nesting of lists/quotes getting swapped when toggling HTML mode
- Disappearing newlines between headings
- Closing lists with double enter adding extra newline
- Text inside paragraph getting moved outside when toggling HTML mode
- Heading inside list & quote adding extra newlines
- Double space to end sentence inserting a space before period
- Using backspace on Nougat causing weird cursor behavior
- Weird cursor behavior when deleting text inside quote
- Heading inside lists adding extra newlines
- Dropped the Merriweather font and use the default serif & monospace fonts
- Updated the Heading icon
- Updated unknown-HTML dialog style to match the WordPress colors
- Fixed the heading-deletion crash
- Fixed a strange behavior when adding multiple different heading

## [0.5.0-alpha.3] - 2017-02-27
- fixed cursor visibility on highlighted background
- fixed broken links on older Android versions
- fixed empty block quotes being removed
- wide images inside lists are now adjusted to fit
- toolbar state is preserved on rotation
- added keyboard shortcuts for format toolbar buttons
- new unknown HTML (question mark icon) edit dialog
- fixed the link crash
- span tag is now formatted inline

## [0.5.0-alpha.2] - 2017-02-13
- a handful of bugfixes

## [0.5.0-alpha] - 2017-02-08
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