# Aztec


[![Build Status](https://travis-ci.org/wordpress-mobile/WordPress-Aztec-Android.svg?branch=develop)](https://travis-ci.org/wordpress-mobile/WordPress-Aztec-Android)

Aztec (extend EditText) is a rich text editor component for writing HTML
documents in Android.

Support Android 4.0+

# Build and test

Build the library, build the example project and run unit tests:

```shell
$ ./gradlew build
```

Run unit tests only:

```shell
$ ./gradlew test
```

Note: there is no instrumentation tests at the moment.

# Integrate in your project

The library is not ready for prime time yet, so it's not distributed on maven
central. Brave developers can either use the project as a source distribution
or have fun with jitpack at their own risk:

```gradle
compile ('com.github.wordpress-mobile.WordPress-Aztec-Android:aztec:develop-SNAPSHOT')
```

When Aztec is ready, we'll publish the artifact in maven.

## Reference

* [Spans, a Powerful Concept](http://flavienlaurent.com/blog/2014/01/31/spans/ "Spans, a Powerful Concept.").
* [Spanned, Android Reference Documentation](http://developer.android.com/reference/android/text/Spanned.html "Spanned | Android Developers").
* Aztec was inspired by [Knife](https://github.com/mthli/Knife).

## License

Aztec is an Open Source project covered by the
[GNU General Public License version 2](LICENSE.md).
