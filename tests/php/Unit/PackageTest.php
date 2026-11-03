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

it('targets the minimum platform versions NativePHP Mobile v4 supports', function () {
    $manifest = PluginManifest::fromFile(packageRoot().'/nativephp.json');

    expect($manifest->android['min_version'])->toBeInt()->toBeGreaterThanOrEqual(29)
        ->and($manifest->ios['min_version'])->toBeString()
        ->and(version_compare($manifest->ios['min_version'], '18.0', '>='))->toBeTrue();
});
