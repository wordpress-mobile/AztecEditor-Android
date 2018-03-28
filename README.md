<h1><img align="center" width=50px height=50px src="https://github.com/wordpress-mobile/AztecEditor-iOS/raw/develop/RepoAssets/aztec.png" alt="Aztec Logo"/>&nbsp;Aztec: Native HTML Editor for Android</h1>

[![Build Status](https://travis-ci.org/wordpress-mobile/AztecEditor-Android.svg?branch=develop)](https://travis-ci.org/wordpress-mobile/AztecEditor-Android)
[![BuddyBuild](https://dashboard.buddybuild.com/api/statusImage?appID=5800168c52aea90100a973ed&branch=develop&build=latest)](https://dashboard.buddybuild.com/apps/5800168c52aea90100a973ed/build/latest)

Aztec (which extends EditText) is a rich-text editor component for writing HTML
documents in Android.

Supports Android 4.1+ (API 16 - Jelly Bean)

<img align="center" width=360px height=640px src="https://github.com/wordpress-mobile/AztecEditor-Android/raw/develop/visual_editor.png" alt="Visual Editor"/> <img align="center" width=360px height=640px src="https://github.com/wordpress-mobile/AztecEditor-Android/raw/develop/code_editor.png" alt="Visual Editor"/>

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

For more options, such as edit history, listeners and plugins please refer to the [demo app implementation](https://github.com/wordpress-mobile/AztecEditor-Android/blob/develop/app/src/main/kotlin/org/wordpress/aztec/demo/MainActivity.kt).

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

You can import Aztec into your project using Jitpack:
```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```
```gradle
dependencies {
    api ('com.github.wordpress-mobile.WordPress-Aztec-Android:aztec:v1.3.1')
}
```

Brave developers can either use the project as a source distribution
or have fun with the latest snapshot at their own risk:

```gradle
dependencies {
    api ('com.github.wordpress-mobile.WordPress-Aztec-Android:aztec:develop-SNAPSHOT')
}
```

## Code formatting

We use [ktlint](https://github.com/shyiko/ktlint) for Kotlin linting. You can run ktlint using `./gradlew ktlint`, and you can also run `./gradlew ktlintFormat` for auto-formatting. There is no IDEA plugin (like Checkstyle's) at this time.

## Reference

* [Spans, a Powerful Concept](http://flavienlaurent.com/blog/2014/01/31/spans/ "Spans, a Powerful Concept.").
* [Spanned, Android Reference Documentation](http://developer.android.com/reference/android/text/Spanned.html "Spanned | Android Developers").
* Aztec was inspired by [Knife](https://github.com/mthli/Knife).

## License

Aztec is an Open Source project covered by the
[GNU General Public License version 2](LICENSE.md).
