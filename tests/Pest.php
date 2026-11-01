<?php

use Jubianchi\NativePHP\LottieRefreshIndicator\Tests\TestCase;

uses(TestCase::class)->in('php/Feature');

function temporaryDirectory(): string
{
    $path = sys_get_temp_dir().'/lottie-refresh-indicator-'.bin2hex(random_bytes(6));
    mkdir($path, 0755, true);

    return $path;
}

/**
 * @param  array<string, string>  $entries
 */
function makeZip(string $path, array $entries): string
{
    if (! is_dir(dirname($path))) {
        mkdir(dirname($path), 0755, true);
    }

    $zip = new ZipArchive;
    $zip->open($path, ZipArchive::CREATE | ZipArchive::OVERWRITE);
    foreach ($entries as $name => $content) {
        $zip->addFromString($name, $content);
    }
    $zip->close();

    return $path;
}

/**
 * @param  array<string, mixed>  $animation
 */
function makeDotLottieV2(string $path, array $animation): string
{
    return makeZip($path, [
        'manifest.json' => json_encode(['version' => '2', 'animations' => [['id' => 'logo']]]),
        'a/logo.json' => json_encode($animation),
    ]);
}

/**
 * @param  array<string, mixed>  $animation
 */
function makeDotLottieV1(string $path, array $animation): string
{
    return makeZip($path, [
        'manifest.json' => json_encode(['version' => '1.0', 'animations' => [['id' => 'main']]]),
        'animations/main.json' => json_encode($animation),
    ]);
}

/**
 * @return array<string, string>
 */
function zipEntries(string $path): array
{
    $zip = new ZipArchive;
    $zip->open($path);
    $entries = [];
    for ($i = 0; $i < $zip->numFiles; $i++) {
        $name = $zip->getNameIndex($i);
        $entries[$name] = $zip->getFromName($name);
    }
    $zip->close();
    ksort($entries);

    return $entries;
}
