## jubianchi/nativephp-lottie-refresh-indicator

A NativePHP Mobile plugin that replaces the pull-to-refresh spinner of `nativephp/mobile-ui` lists with a Lottie
animation, on Android and iOS. It registers an indicator named `lottie` in mobile-ui's pull-to-refresh indicator
registry, which no published version of mobile-ui ships yet: the plugin can't be used with a released mobile-ui.

### Installation

```bash
composer require jubianchi/nativephp-lottie-refresh-indicator
php artisan native:plugin:register jubianchi/nativephp-lottie-refresh-indicator
```

### Usage

Name the `lottie` indicator on a `native:list` that has `on-refresh`, and pass the animation in
`refresh-indicator-options`:

@verbatim
<code-snippet name="Lottie pull-to-refresh indicator on a Blade list" lang="blade">
<native:list
    on-refresh="reload"
    refresh-indicator="lottie"
    :refresh-indicator-options="[
        'asset' => 'animations/refresh.lottie',
        'dark_asset' => 'animations/refresh-dark.lottie',
    ]"
>
    {{-- rows --}}
</native:list>
</code-snippet>
@endverbatim

@verbatim
<code-snippet name="Lottie pull-to-refresh indicator on a PHP list element" lang="php">
NativeList::make()
    ->onRefresh('reload')
    ->refreshIndicator('lottie', [
        'asset' => 'animations/refresh.lottie',
        'dark_asset' => 'animations/refresh-dark.lottie',
    ]);
</code-snippet>
@endverbatim

### Options

- `asset` (required): path of the animation, a dotLottie (`.lottie`) or Lottie JSON (`.json`) file, such as
  `animations/refresh.lottie`. Without it, the indicator draws nothing.
- `dark_asset` (optional): animation to play instead in dark mode.

### Animation files

- Put the animations in the app's `resources/animations` directory (or the directory set by
  `LOTTIE_REFRESH_INDICATOR_PATH` / the `lottie-refresh-indicator.path` config). The plugin's `copy_assets` hook
  copies every `.lottie` and `.json` file there under `animations/` in the native projects when the app is built.
- An `asset` is the file's path in that directory prefixed with `animations/`: `resources/animations/brand/logo.lottie`
  is `animations/brand/logo.lottie`.
- dotLottie v2 files are converted to v1 for iOS. iOS bundles resources flat, so two animations with the same file name
  in different directories clash there: give them different names.

### Markers

- A `pull` marker sets the part of the animation the pull plays, from rest to the refresh threshold.
- A `refresh` marker sets the part the refresh loops over, always forward; make its last frame look like its first.
- `refresh` should start where `pull` ends. Without `pull`, the pull plays what comes before `refresh`.
- Without a `refresh` marker, the refresh plays the pull part back and forth instead of looping it forward.
- Name markers in lowercase: lottie-ios matches marker names exactly, including case, while lottie-android ignores
  case.

### Gotchas

- No JavaScript API, no bridge functions, no events, no permissions, no environment variables or secrets: the list's own
  `on-refresh` handler runs the refresh, and the indicator is configured on the list element only.
- If the plugin isn't installed, a list naming `lottie` keeps the platform spinner.
