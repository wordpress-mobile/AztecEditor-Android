# Instructions

This file provides guidance to coding agents when working with code in this repository.

## General Approach

- Analyze assumptions and provide counterpoints — prioritize truth over agreement.
- Treat me as an expert Android developer. Give overviews, not tutorials.
- Always give an overview of the solution before diving into implementation (unless explicitly asked to implement right away).
- Do not add comments for trivial logic or when the name is descriptive enough.

## Architecture Overview

**AztecEditor-Android** is a rich-text HTML editor library for Android, built on the `EditText` / `Spannable` API.

**Stack**: Kotlin, Android Spannable API, Plugin architecture, Gradle with Kotlin DSL

### Module Structure

```
:app                     — Demo application
:aztec                   — Core editor library (main deliverable)
:glide-loader            — Image loading via Glide
:picasso-loader          — Image loading via Picasso
:wordpress-comments      — WordPress comments plugin
:wordpress-shortcodes    — WordPress shortcodes plugin
:media-placeholders      — Media placeholders with Compose support
```

All plugin/loader modules depend on `:aztec`. Published independently to Automattic S3 Maven (`org.wordpress:aztec`, `org.wordpress.aztec:*`).

### Code Navigation (package: `org.wordpress.aztec`)

| To find...                      | Look in...                                           |
|---------------------------------|------------------------------------------------------|
| Main editor component           | `aztec/.../AztecText.kt` (extends EditText, ~3K LOC) |
| Editor facade / initialization  | `aztec/.../Aztec.kt`                                 |
| HTML parsing                    | `aztec/.../AztecParser.kt`, `AztecTagHandler.kt`     |
| Custom spans (visual rendering) | `aztec/.../spans/` (62 files — one per HTML element)  |
| Text formatting logic           | `aztec/.../formatting/` (Block, Inline, Line, List)   |
| Block element handlers          | `aztec/.../handlers/` (Heading, List, Quote, etc.)    |
| Plugin interfaces               | `aztec/.../plugins/` (IAztecPlugin, IToolbarButton)   |
| HTML source editor              | `aztec/.../source/SourceViewEditText.kt`              |
| Text change watchers            | `aztec/.../watchers/` (30+ files, API-version buckets)|
| Toolbar                         | `aztec/.../toolbar/AztecToolbar.kt`                   |
| Undo/redo                       | `aztec/.../History.kt`                                |
| Compose placeholders            | `media-placeholders/.../ComposePlaceholder*.kt`       |

### Key Architectural Patterns

1. **Span-based rendering** — Each HTML element has a custom `Span` class. Spans carry both visual rendering and semantic meaning. The `Spannable` API is central to everything.

2. **Plugin architecture** — `IAztecPlugin` with sub-interfaces (`IToolbarButton`, `IClipboardPastePlugin`, `IOnDrawPlugin`). Plugins for `html2visual` and `visual2html` conversion pipelines.

3. **Facade pattern** — `Aztec` class provides a builder-like fluent API to wire up editor, toolbar, source view, and plugins.

4. **Handler/Formatter split** — Handlers deal with block element structure (headings, lists, quotes). Formatters apply styling (inline, block, line-block, list, link, indent).

5. **Bidirectional HTML conversion** — HTML string <-> Spannable representation, with plugin hooks at each stage.

6. **Watcher pattern** — Multiple `TextWatcher` implementations with API-version-specific buckets (API 25, 26+) for Samsung/OEM compatibility.

### Common Crash Patterns (from recent PRs)

- `IndexOutOfBoundsException` from span start/end being out of bounds — always clamp to `[0, text.length]`
- `IllegalArgumentException` when span end < span start — validate before applying
- Samsung/custom emoji OEM issues causing unexpected span states
- Thread safety in `AztecAttributes` — operations should be synchronized

## Git Operations (CRITICAL)
- **NEVER commit without explicit permission**
- **NEVER push without explicit permission**
- **NEVER create a PR without explicit permission**
- When asked to "fix" or "update" something, that does NOT imply permission to commit/push
- Always wait for explicit "commit", "push", or "create PR" commands
- Do not create branches, rebase, or modify git history unless explicitly asked

## Build Commands

```bash
# Build and run the demo app
./gradlew :app:installDebug && adb shell am start -n org.wordpress.aztec/org.wordpress.aztec.demo.MainActivity

# Build the demo app (without installing)
./gradlew :app:assembleDebug

# Build the library
./gradlew :aztec:assembleRelease

# Lint (ktlint + Android lint)
./gradlew ktlint
./gradlew lintRelease

# Unit tests (core library)
./gradlew aztec:testRelease

# All unit tests
./gradlew testRelease

# Specific test class
./gradlew :aztec:testReleaseUnitTest --tests "org.wordpress.aztec.AztecParserTest" --info

# Specific test method
./gradlew :aztec:testReleaseUnitTest --tests "org.wordpress.aztec.AztecParserTest.testMethodName" --info
```

### Build Configuration

- All Kotlin warnings are errors (`allWarningsAsErrors = true`)
- Versions are defined in root `build.gradle` — check there for current Kotlin, AGP, SDK, and dependency versions

## GitHub Commands

```bash
PAGER=cat gh pr list
PAGER=cat gh pr view <NUMBER>
PAGER=cat gh pr view <NUMBER> --comments
PAGER=cat gh pr diff <NUMBER>
```

## PR Template

PRs follow this format (from `.github/PULL_REQUEST_TEMPLATE.md`):
```
### Fix
<description of what was fixed/added>

### Test
1. Step 1
2. Step 2

### Review
@reviewer
```

## Skills

| Skill       | Trigger phrases                                                                                      |
|-------------|------------------------------------------------------------------------------------------------------|
| `implement` | "implement", "fix this", "work on this", "build this", "add feature", any implementation request     |
