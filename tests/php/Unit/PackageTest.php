<?php

use Native\Mobile\Plugins\PluginManifest;

function packageRoot(): string
{
    return dirname(__DIR__, 3);
}

it('requires the NativePHP Mobile core directly', function () {
    $composer = json_decode(file_get_contents(packageRoot().'/composer.json'), true);

    expect($composer['require'])->toHaveKey('nativephp/mobile', '^4.7');
});

it('keeps native sources in the directories the marketplace expects', function () {
    $root = packageRoot();

    expect(glob("{$root}/resources/ios/Sources/*.swift"))->not->toBeEmpty()
        ->and(glob("{$root}/resources/android/src/*.kt"))->not->toBeEmpty()
        ->and(glob("{$root}/resources/ios/*.swift"))->toBeEmpty()
        ->and(glob("{$root}/resources/android/*.kt"))->toBeEmpty();
});

it('targets the minimum platform versions NativePHP Mobile v4 supports', function () {
    $manifest = PluginManifest::fromFile(packageRoot().'/nativephp.json');

    expect($manifest->android['min_version'])->toBeInt()->toBeGreaterThanOrEqual(29)
        ->and($manifest->ios['min_version'])->toBeString()
        ->and(version_compare($manifest->ios['min_version'], '18.0', '>='))->toBeTrue();
});
