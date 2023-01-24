<h1><img align="center" width=50px height=50px src="https://github.com/wordpress-mobile/AztecEditor-Android/raw/trunk/RepoAssets/aztec.png" alt="Aztec Logo"/>&nbsp;Aztec: Native HTML Editor for Android</h1>

[![CircleCI](https://circleci.com/gh/wordpress-mobile/AztecEditor-Android.svg?style=svg)](https://circleci.com/gh/wordpress-mobile/AztecEditor-Android)

Aztec (which extends EditText) is a rich-text editor component for writing HTML
documents in Android.

Supports Android 4.1+ (API 16 - Jelly Bean)

<img align="center" width=360px height=640px src="https://github.com/wordpress-mobile/AztecEditor-Android/raw/trunk/visual_editor.png" alt="Visual Editor"/> <img align="center" width=360px height=640px src="https://github.com/wordpress-mobile/AztecEditor-Android/raw/trunk/code_editor.png" alt="Visual Editor"/>

## Getting started

Declare the main components in your layout:

Visual editor
```XML
<org.wordpress.aztec.AztecText
    android:id="@+id/visual"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scrollbars="vertical"
    android:imeOptions="flagNoExtractUi"
    aztec:historyEnable="false" />
```
Source editor
```XML
<org.wordpress.aztec.source.SourceViewEditText
    android:id="@+id/source"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:inputType="textNoSuggestions|textMultiLine"
    android:scrollbars="vertical"
    android:imeOptions="flagNoExtractUi"
    aztec:codeBackgroundColor="@android:color/transparent"
    aztec:codeTextColor="@android:color/white" />
```

Toolbar
```XML
<org.wordpress.aztec.toolbar.AztecToolbar
    android:id="@+id/formatting_toolbar"
    android:layout_width="match_parent"
    android:layout_height="@dimen/format_bar_height"
    android:layout_alignParentBottom="true" />
```

Inflate the views:
```kotlin
val visualEditor = findViewById<AztecText>(R.id.visual)
val sourceEditor = findViewById<SourceViewEditText>(R.id.source)
val toolbar = findViewById<AztecToolbar>(R.id.formatting_toolbar)
```

Configure Aztec with a provided image & video loaders:
```kotlin
Aztec.with(visualEditor, sourceEditor, toolbar, context)
    .setImageGetter(GlideImageLoader(context))
    .setVideoThumbnailGetter(GlideVideoThumbnailLoader(context))
```

For more options, such as edit history, listeners and plugins please refer to the [demo app implementation](https://github.com/wordpress-mobile/AztecEditor-Android/blob/trunk/app/src/main/kotlin/org/wordpress/aztec/demo/MainActivity.kt).

## Build and test

Build the library, build the example project and run unit tests:

```shell
$ ./gradlew build
```

Run unit tests only:

```shell
$ ./gradlew test
```

### Before running instrumentation tests

Espresso [advises](https://google.github.io/android-testing-support-library/docs/espresso/setup/#setup-your-test-environment) disabling system animations on devices used for testing:

> On your device, under Settings->Developer options disable the following 3 settings:
>
> - Window animation scale
> - Transition animation scale
> - Animator duration scale

One additional setup step is also required to handle an Espresso issue with clicks (see the caveats below):

On your device, under Settings -> Accessibility -> Touch & hold delay, set the delay to `Long`.

Run the instrumentation tests:

```shell
$ ./gradlew cAT
```

## Integrating Aztec in your project

```gradle
repositories {
    maven { url "https://a8c-libs.s3.amazonaws.com/android" }
}
```
```gradle
dependencies {
    api "org.wordpress:aztec:v1.6.2"
}
```

Brave developers can either use the project as a source distribution
or have fun with the latest snapshot at their own risk:

```gradle
dependencies {
    api "org.wordpress:aztec:trunk-{commit_sha1}"
    // As an example, for '3f004c8c8cd4b53ab9748f42f373cf00a30e9d86' commit sha1, this would look like:
    // api "org.wordpress:aztec:trunk-3f004c8c8cd4b53ab9748f42f373cf00a30e9d86"
}
```

## Modifications

You can use the API to modify Aztec behaviour. 

### Toolbar items

If you want to limit the functionality the Aztec library provides, you can change it calling the `setToolbarItems` method on `AztecToolbar`. 
The following example will enable only `bold`, `plugins` and `list` items in the given order.

```kotlin
aztecToolbar.setToolbarItems(ToolbarItems.BasicLayout(ToolbarAction.BOLD, ToolbarItems.PLUGINS, ToolbarAction.LIST))
```

You can set new items which are not enabled by default. `ToolbarAction.CODE` and `ToolbarAction.PRE`. 
- `CODE` represents inline HTML code 
- `PRE` represents a preformat block (including code block)

### Task list

There is an optional list type you can enable in the editor. In addition to ordered and unordered lists you can use `task list`. 
A task list is an unordered list which shows and saves checkboxes instead of the bullets. Enable it by calling the following method.
```kotlin
aztecToolbar.enableTaskList()
```

### Nested blocks

By default Aztec allows nested blocks. In certain cases this doesn't have to be the preferred behaviour. There is an option to disable nested blocks.
When switched, this editor will always add media and horizontal rule after the currently selected block, not in the middle of it.
```kotlin
aztecText.addMediaAfterBlocks()
```

### Placeholder API

Aztec now supports placeholders to draw views which are not natively supported by the EditText and Spannable API. 
The functionality creates a span in the visual editor and draws an Android view over it. 
The view is moved around when the user changes anything in the editor and allows you to draw things like video that can be played inline in the editor.
In order to use the API you have to create an instance of `PlaceholderManager` and initialize it in your `onCreate` call like this: 

```kotlin
private lateinit var placeholderManager: PlaceholderManager
override fun onCreate(savedInstanceState: Bundle?) {
    placeholderManager = PlaceholderManager(visualEditor, findViewById(R.id.container_frame_layout))
    aztec.addPlugin(placeholderManager)
    aztec.addOnMediaDeletedListener(placeholderManager)
}
override fun onDestroy() {
    placeholderManager.onDestroy()
}
```

You can create a custom `PlaceholderAdapter` to prepare and draw your view. 
You can check the sample `ImageWithCaptionAdapter` which draws a simple Android view with an image and a caption. 
However, you can implement things like `YouTube` view or `Video` view with playback controls.
Don't forget to register your `PlaceholderAdapter` like this: 

```kotlin
placeholderManager.registerAdapter(ImageWithCaptionAdapter())
```

Once you have initialized both the manager and the adapter, you can use the manager methods to insert or remove placeholders.

```kotlin
placeholderManager.insertItem(adapterType, attributes)
placeholderManager.removeItem(predicate)
```


## Code formatting

We use [ktlint](https://github.com/shyiko/ktlint) for Kotlin linting. You can run ktlint using `./gradlew ktlint`, and you can also run `./gradlew ktlintFormat` for auto-formatting. There is no IDEA plugin (like Checkstyle's) at this time.

## Reference

* [Spans, a Powerful Concept](http://flavienlaurent.com/blog/2014/01/31/spans/ "Spans, a Powerful Concept.").
* [Spanned, Android Reference Documentation](http://developer.android.com/reference/android/text/Spanned.html "Spanned | Android Developers").
* Aztec was inspired by [Knife](https://github.com/mthli/Knife).

## License

Aztec is an open source project covered by the [Mozilla Public License Version 2.0](LICENSE.md).
