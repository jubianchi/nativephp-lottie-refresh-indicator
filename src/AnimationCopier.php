<?php

namespace Jubianchi\NativePHP\LottieRefreshIndicator;

use RecursiveDirectoryIterator;
use RecursiveIteratorIterator;
use SplFileInfo;

/**
 * Copies the app's animations into the native project, under `animations/`
 * in the Android assets and the iOS bundle resources, so an `asset` option
 * such as `animations/splash.lottie` names the same file on both platforms.
 */
final class AnimationCopier
{
    private const EXTENSIONS = ['lottie', 'json'];

    private const DESTINATIONS = [
        'android' => 'app/src/main/assets',
        'ios' => 'NativePHP/Resources',
    ];

    public static function copy(string $source, string $buildPath, string $platform): AnimationCopyResult
    {
        $root = self::DESTINATIONS[$platform] ?? null;
        if ($root === null || ! is_dir($source)) {
            return new AnimationCopyResult([], []);
        }

        $copied = [];
        $warnings = [];
        foreach (self::animations($source) as $relative) {
            $asset = "animations/{$relative}";
            $destination = "{$buildPath}/{$root}/{$asset}";
            if (! is_dir(dirname($destination))) {
                mkdir(dirname($destination), 0755, true);
            }

            $prepared = "{$destination}.tmp";
            if ($platform === 'ios' && DotLottie::isV2("{$source}/{$relative}")) {
                if (! DotLottie::toV1("{$source}/{$relative}", $prepared)) {
                    $warnings[] = "{$asset} is a dotLottie v2 file that could not be converted for iOS: copied as is, lottie-ios may not play it.";
                    copy("{$source}/{$relative}", $prepared);
                }
            } else {
                copy("{$source}/{$relative}", $prepared);
            }

            if (is_file($destination) && ! DotLottie::sameContent($prepared, $destination)) {
                $warnings[] = "{$asset} replaces a different file already in the {$platform} project, probably copied by another plugin.";
            }
            rename($prepared, $destination);
            $copied[] = $asset;
        }

        if ($platform === 'ios') {
            $warnings = [...$warnings, ...self::flattenedClashes($copied)];
        }

        return new AnimationCopyResult($copied, $warnings);
    }

    /**
     * @return list<string>
     */
    private static function animations(string $source): array
    {
        $files = [];
        $iterator = new RecursiveIteratorIterator(new RecursiveDirectoryIterator($source, RecursiveDirectoryIterator::SKIP_DOTS));
        /** @var SplFileInfo $file */
        foreach ($iterator as $file) {
            if ($file->isFile() && in_array(strtolower($file->getExtension()), self::EXTENSIONS, true)) {
                $files[] = substr($file->getPathname(), strlen($source) + 1);
            }
        }
        sort($files);

        return $files;
    }

    /**
     * The app's Xcode project copies resources flat into the bundle, so
     * files sharing a name in different directories clash there.
     *
     * @param  list<string>  $assets
     * @return list<string>
     */
    private static function flattenedClashes(array $assets): array
    {
        $byName = [];
        foreach ($assets as $asset) {
            $byName[basename($asset)][] = $asset;
        }

        $warnings = [];
        foreach ($byName as $name => $paths) {
            if (count($paths) > 1) {
                $warnings[] = 'iOS bundles resources flat, so '.implode(', ', $paths)." clash as {$name}: give them different names.";
            }
        }

        return $warnings;
    }
}
