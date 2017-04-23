<h1><img align="center" width=50px height=50px src="https://github.com/wordpress-mobile/AztecEditor-iOS/raw/develop/RepoAssets/aztec.png" alt="Aztec Logo'"/>&nbsp;Aztec: Native HTML Editor for Android</h1>

[![Build Status](https://travis-ci.org/wordpress-mobile/AztecEditor-Android.svg?branch=develop)](https://travis-ci.org/wordpress-mobile/AztecEditor-Android)
[![BuddyBuild](https://dashboard.buddybuild.com/api/statusImage?appID=5800168c52aea90100a973ed&branch=develop&build=latest)](https://dashboard.buddybuild.com/apps/5800168c52aea90100a973ed/build/latest)

Aztec (which extends EditText) is a rich-text editor component for writing HTML
documents in Android.

Supports Android 4.0+

## Build and test

Build the library, build the example project and run unit tests:

```shell
$ ./gradlew build
```

Run unit tests only:

```shell
$ ./gradlew test
```

Note: There are no instrumentation tests at the moment.

## Integrating Aztec in your project

The library is not ready for prime time yet, so it's not published on Maven
Central. Currently the library is distributed as an [alpha build](https://github.com/wordpress-mobile/AztecEditor-Android/releases).

Brave developers can either use the project as a source distribution
or have fun with JitPack at their own risk:

```gradle
compile ('com.github.wordpress-mobile.WordPress-Aztec-Android:aztec:develop-SNAPSHOT')
```

When Aztec is ready, we'll publish the artifact in Maven.

## Reference

* [Spans, a Powerful Concept](http://flavienlaurent.com/blog/2014/01/31/spans/ "Spans, a Powerful Concept.").
* [Spanned, Android Reference Documentation](http://developer.android.com/reference/android/text/Spanned.html "Spanned | Android Developers").
* Aztec was inspired by [Knife](https://github.com/mthli/Knife).

## License

Aztec is an Open Source project covered by the
[GNU General Public License version 2](LICENSE.md).
