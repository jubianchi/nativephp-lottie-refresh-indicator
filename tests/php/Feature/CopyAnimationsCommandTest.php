<?php

use Illuminate\Support\Facades\File;
use Native\Mobile\Plugins\PluginManifest;

beforeEach(function () {
    $this->source = temporaryDirectory();
    $this->build = temporaryDirectory();
});

it('reads the animations from resources/animations by default', function () {
    expect(config('lottie-refresh-indicator.path'))->toBe('resources/animations');
});

it('is the plugin\'s copy_assets hook', function () {
    $manifest = PluginManifest::fromFile(__DIR__.'/../../../nativephp.json');

    expect($manifest->hooks['copy_assets'] ?? null)->toBe('nativephp:lottie-refresh-indicator:copy-assets');
});

it('copies the configured animations into the native project', function () {
    makeDotLottieV1("{$this->source}/splash.lottie", ['layers' => []]);
    config(['lottie-refresh-indicator.path' => $this->source]);

    $this->artisan('nativephp:lottie-refresh-indicator:copy-assets', ['--platform' => 'android', '--build-path' => $this->build])
        ->expectsOutputToContain('animations/splash.lottie')
        ->assertSuccessful();

    expect("{$this->build}/app/src/main/assets/animations/splash.lottie")->toBeFile();
});

it('reads a relative path from the app\'s root', function () {
    $relative = 'storage/lottie-'.bin2hex(random_bytes(4));
    makeDotLottieV1(base_path("{$relative}/splash.lottie"), ['layers' => []]);
    config(['lottie-refresh-indicator.path' => $relative]);

    $this->artisan('nativephp:lottie-refresh-indicator:copy-assets', ['--platform' => 'ios', '--build-path' => $this->build])
        ->assertSuccessful();

    expect("{$this->build}/NativePHP/Resources/animations/splash.lottie")->toBeFile();

    File::deleteDirectory(base_path($relative));
});

it('reports the warnings', function () {
    makeDotLottieV1("{$this->source}/light/logo.lottie", ['layers' => []]);
    makeDotLottieV1("{$this->source}/dark/logo.lottie", ['layers' => []]);
    config(['lottie-refresh-indicator.path' => $this->source]);

    $this->artisan('nativephp:lottie-refresh-indicator:copy-assets', ['--platform' => 'ios', '--build-path' => $this->build])
        ->expectsOutputToContain('clash as logo.lottie')
        ->assertSuccessful();
});

it('skips an app without animations directory', function () {
    config(['lottie-refresh-indicator.path' => "{$this->source}/missing"]);

    $this->artisan('nativephp:lottie-refresh-indicator:copy-assets', ['--platform' => 'android', '--build-path' => $this->build])
        ->expectsOutputToContain('No animations directory')
        ->assertSuccessful();
});
