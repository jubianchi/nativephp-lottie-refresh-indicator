# nativephp-lottie-refresh-indicator

A [NativePHP Mobile](https://nativephp.com) plugin that replaces the pull-to-refresh spinner of
[nativephp/mobile-ui](https://github.com/NativePHP/mobile-ui) lists with a [Lottie](https://airbnb.io/lottie/)
animation, on Android and iOS.

While the list is pulled, the animation follows the pull: a logo that draws itself in is fully drawn when the pull
reaches the refresh threshold. While the list refreshes, the animation plays back and forth from where the pull left
it, so it never jumps back to its first frame: a logo that draws itself in undraws and redraws instead of blanking.

> [!IMPORTANT]
> This plugin registers an indicator named `lottie` with mobile-ui's pull-to-refresh indicator registry. That registry
> is not released yet: it lives on mobile-ui's `feat/refresh-indicator-registry` branch. Until it is released, install
> mobile-ui from that branch (see [Installation](#installation)).

## Requirements

- `nativephp/mobile` 4.7 or later
- `nativephp/mobile-ui` with the indicator registry (branch `feat/refresh-indicator-registry`)
- Android 8.0 (API 26) or later, iOS 18.2 or later
- The animations, in the app's Android assets and iOS bundle (see [Animation files](#animation-files))

## Installation

Neither this plugin nor mobile-ui's indicator registry is on Packagist yet, so add both as
[path repositories](https://getcomposer.org/doc/05-repositories.md#path) in your app's `composer.json`, pointing at
local checkouts:

```json
{
    "repositories": [
        {
            "type": "path",
            "url": "../mobile-ui"
        },
        {
            "type": "path",
            "url": "../nativephp-lottie-refresh-indicator"
        }
    ]
}
```

`../mobile-ui` must have the `feat/refresh-indicator-registry` branch checked out (a git worktree of that branch works
too). Then require both at their development version and register the plugin with NativePHP:

```bash
composer require "nativephp/mobile-ui:@dev" "jubianchi/nativephp-lottie-refresh-indicator:@dev"
php artisan native:plugin:register jubianchi/nativephp-lottie-refresh-indicator
```

If your app already requires a released mobile-ui (for example `^0.9`), the `@dev` requirement replaces it. The plugin
itself requires `nativephp/mobile-ui` with no version constraint, because Composer sees a path repository's branch as a
`dev-…` version that no version range would accept; it will require the first release that ships the registry once it
exists.

## Usage

Name the `lottie` indicator on a list that refreshes, and give it the animation to play:

```blade
<native:list
    on-refresh="reload"
    refresh-indicator="lottie"
    :refresh-indicator-options="[
        'asset' => 'animations/splash.lottie',
        'dark_asset' => 'animations/splash-dark.lottie',
    ]"
>
    {{-- rows --}}
</native:list>
```

The same from PHP:

```php
NativeList::make()
    ->onRefresh('reload')
    ->refreshIndicator('lottie', [
        'asset' => 'animations/splash.lottie',
        'dark_asset' => 'animations/splash-dark.lottie',
    ]);
```

If the app doesn't install this plugin, the list keeps the platform spinner.

### Options

| Option       | Required | Description                                                                                  |
|--------------|----------|----------------------------------------------------------------------------------------------|
| `asset`      | Yes      | Path of the animation, a dotLottie (`.lottie`) or Lottie JSON (`.json`) file.                 |
| `dark_asset` | No       | Animation to play instead of `asset` in dark mode. Defaults to `asset`.                       |

Without `asset`, the indicator draws nothing: the list still refreshes, with an empty indicator.

### Animation files

The plugin doesn't ship or copy animations: they must already be in the app, under the path `asset` gives.

- **Android** loads `asset` from the APK's assets, so `animations/splash.lottie` is
  `app/src/main/assets/animations/splash.lottie` in the generated Android project.
- **iOS** looks `asset` up in the app bundle, first in its own directory, then at the root of the bundle, with the same
  name and extension. The generated Xcode project copies resources flat, so a file placed at
  `NativePHP/Resources/animations/splash.lottie` ends up as `splash.lottie` at the root of the bundle, and
  `animations/splash.lottie` still finds it.

The `s2br/nativephp-mobile-splashscreen` plugin copies its splash animations to exactly these places (as
`animations/<name>.lottie`), so a refresh indicator can reuse the splash animation with
`'asset' => 'animations/splash.lottie'`. That plugin also converts dotLottie v2 files to v1 for iOS, as it finds
lottie-ios doesn't play v2 files: an animation you ship yourself should be a dotLottie v1 or a JSON file to play on iOS.

## Platform notes

### Android

- The indicator is drawn with [lottie-compose](https://github.com/airbnb/lottie-android) **6.7.1**, the version the
  splashscreen plugin uses, so both share one dependency.
- mobile-ui draws the disc around the indicator (48dp, with 6dp padding): the animation fills the remaining 36dp.
- Dark mode follows the system setting (`isSystemInDarkTheme()`).
- The pull is reported to accessibility services as a progress, indeterminate while refreshing.

### iOS

- The indicator is drawn with [lottie-ios](https://github.com/airbnb/lottie-ios), added from
  `https://github.com/airbnb/lottie-spm.git` with **4.6.0** as the minimum version (up to the next major), the same
  package and version as the splashscreen plugin: NativePHP adds a package once per URL, so both share it.
- The list draws the indicator in a 40pt square in the gap the pull opens above its rows.
- Dark mode follows the SwiftUI color scheme of the screen.
- The animation is loaded synchronously the first time the list is pulled, then from Lottie's cache.

## Development

The plugin's logic is tested on both platforms. Neither suite renders a Lottie animation: Robolectric can't load real
Lottie assets, and the iOS suite runs on macOS without UIKit.

### Android

A standalone Robolectric module compiles the plugin's Kotlin sources with mobile-ui's indicator registry, taken from a
mobile-ui checkout. It requires JDK 21 and the Android SDK 36, and runs with the Gradle wrapper of a NativePHP Mobile
checkout:

```bash
/path/to/mobile/resources/androidstudio/gradlew \
  -p tests/android -PmobileUiRoot=/absolute/path/to/mobile-ui testDebugUnitTest
```

### iOS

A Swift package links the plugin's plain Swift logic (asset choice, bundle lookup, playback) and tests it with Swift
Testing, on macOS with Xcode 16 or later:

```bash
swift test --package-path tests/ios
```

The SwiftUI view needs UIKit and Lottie, so it is only typechecked, together with the core app and mobile-ui, against
a Lottie framework built for the iOS simulator (for example the one an app build downloads into
`SourcePackages/artifacts/lottie-spm`):

```bash
tests/ios/typecheck/typecheck.sh /path/to/mobile-ui /path/to/mobile \
  /path/to/Lottie.xcframework/ios-arm64_x86_64-simulator
```
