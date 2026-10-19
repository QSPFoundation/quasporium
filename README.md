# Quasporium

An Android player for [QSP](https://qsp.org) text games: browse the qsp.org catalogue, install
games in one tap and play them with either of the two QSP engines.

<a href="https://play.google.com/store/apps/details?id=com.pixnpunk.quasporium">Get it on Google Play</a>

## Features

- **Catalogue** of qsp.org: search by title, author and translator, sorting, language filter.
- **One-tap install**: the game archive is downloaded and unpacked in the background.
- **Your own games**: add a folder with a `.qsp` or `.gam` file from the device storage.
- **Two engines**: QSP 5.9.5 ([qsp](https://github.com/QSPFoundation/qsp)) and QSP 5.7.0
  ([qsp-legacy](https://github.com/QSPFoundation/qsp-legacy)). The engine is chosen globally or per game.
- **Saves** with your own names, and progress restored if Android closes the app in the background.
- **Per-game settings**: font and custom fonts, text size, text, link and background colours, image size.
- HTML games (`USEHTML`), sound, immersive mode, light and dark theme.

Android 8.0 (API 26) or newer.

## Builds

| Flavor | Where | What is different |
|---|---|---|
| `foss` | GitHub Releases | No proprietary code and no telemetry: errors are only written to logcat |
| `play` | Google Play | Firebase Crashlytics, Analytics and Performance Monitoring |

Both flavors use the same application id, `com.pixnpunk.quasporium`. They are signed with
different keys, so switching between them requires uninstalling the app first.

## Building

Requirements:

- JDK 17 or newer
- Android SDK 37, NDK 27.3.13750724 and CMake 3.22.1 (install them from the SDK Manager if the build does not download them)
- Git and network access: the QSP engines and Oniguruma are fetched by CMake at pinned commits

```bash
git clone <repository URL>
cd Quasporium
./gradlew :app:assembleFossDebug
```

The APK is in `app/build/outputs/apk/foss/debug/`. `foss` is the default variant in Android Studio.

The `play` flavor additionally needs `app/google-services.json` from the Firebase console. The file
is not in the repository and is ignored by git.

Unit tests:

```bash
./gradlew testFossDebugUnitTest testDebugUnitTest
```

## Project structure

The app is split into Gradle modules by feature and layer:

- `app` — application class, root navigation, flavor-specific code (`src/foss`, `src/play`)
- `core:*` — database (Room), network (Ktor), logging interface, notifications; `core:common`
  (pure Kotlin helpers), `core:storage` (SAF and archive unpacking), `core:ui` (Compose helpers)
- `features:<name>:{domain,data,presentation}` — catalogue (`stock`), game details, game screen and
  engine bridge (`game`), settings, dialogs, WebView rendering
- `build-logic` — Gradle convention plugins (`quasporium.android.library`, `quasporium.android.feature`,
  `quasporium.jvm.library` and others) that hold the configuration shared by the modules

Stack: Kotlin, Jetpack Compose, Decompose, MVIKotlin, Koin, Room, Ktor, Coil, WorkManager,
SimpleStorage (SAF) and the native QSP engines built with CMake.

## Contributing

Bug reports, game compatibility reports and pull requests are welcome; see
[CONTRIBUTING.md](CONTRIBUTING.md). Issues can be written in English or Russian.

Please report security problems privately, as described in [SECURITY.md](SECURITY.md).

## License

Copyright (C) 2026 the Quasporium contributors

Quasporium is free software: you can redistribute it and/or modify it under the terms of the
[GNU General Public License version 3](LICENSE), with an
[additional permission](LICENSE-EXCEPTION.md) for linking with 7-Zip-JBinding-4Android and the
Google Firebase SDKs.

Third-party components keep their own licenses:

| Component | License |
|---|---|
| [qsp](https://github.com/QSPFoundation/qsp) (QSP 5.9.5 engine) | MPL-2.0, parts BSD-2-Clause and BSD-3-Clause |
| [qsp-legacy](https://github.com/QSPFoundation/qsp-legacy) (QSP 5.7.0 engine) | LGPL-2.1-or-later |
| [Oniguruma](https://github.com/kkos/oniguruma) | BSD-2-Clause |
| [7-Zip-JBinding-4Android](https://github.com/omicronapps/7-Zip-JBinding-4Android) | LGPL-2.1 with the unRAR license restriction |
| AndroidX, Kotlin, Ktor, Koin, Coil, Decompose, MVIKotlin, SimpleStorage and other libraries | Apache-2.0 (jsoup: MIT) |
| Firebase SDKs (`play` flavor only) | Proprietary |
