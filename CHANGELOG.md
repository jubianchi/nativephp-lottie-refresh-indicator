# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

The first release waits for a published `nativephp/mobile-ui` with the pull-to-refresh indicator registry.

### Added

- A `lottie` pull-to-refresh indicator for mobile-ui lists, on Android (lottie-compose 6.7.1) and iOS (lottie-ios from
  4.6.0), registered in mobile-ui's indicator registry.
- The `asset` option, a dotLottie or Lottie JSON animation, and the `dark_asset` option for dark mode.
- `pull` and `refresh` animation markers: the pull plays the `pull` part, then the refresh loops the `refresh` part
  forward, carrying on from where the pull left the animation without a jump. Without a `refresh` marker, the refresh
  plays the pull part back and forth.
- A `copy_assets` hook copying the app's animations from `resources/animations` (configurable with
  `LOTTIE_REFRESH_INDICATOR_PATH`) into the Android assets and the iOS bundle, converting dotLottie v2 files to v1 for
  iOS.
- Boost guidelines for AI assistants.
