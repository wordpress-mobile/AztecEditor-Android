# Changelog


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
- Fixed conflicts of autocorrect/suggestions with editor styling

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
