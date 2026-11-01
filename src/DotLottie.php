<?php

namespace Jubianchi\NativePHP\LottieRefreshIndicator;

use ZipArchive;

/**
 * dotLottie handling for lottie-ios, which only reads v1 files. The
 * conversion is the s2br/nativephp-mobile-splashscreen plugin's, so a file
 * both plugins copy ends up with the same content whichever runs last.
 */
final class DotLottie
{
    public static function isV2(string $path): bool
    {
        $manifest = self::entries($path)['manifest.json'] ?? null;
        if ($manifest === null) {
            return false;
        }

        return (json_decode($manifest, true)['version'] ?? '1.0') !== '1.0';
    }

    public static function toV1(string $source, string $destination): bool
    {
        $animation = null;
        foreach (self::entries($source) ?? [] as $name => $content) {
            if (str_starts_with($name, 'a/') && str_ends_with($name, '.json')) {
                $animation = $content;
                break;
            }
        }

        $data = $animation === null ? null : json_decode($animation, true);
        if (! is_array($data)) {
            return false;
        }

        unset($data['fonts']);
        foreach ($data['layers'] ?? [] as $index => $layer) {
            unset($data['layers'][$index]['ef'], $data['layers'][$index]['hasMask']);
        }
        $data['layers'] = array_values(array_filter(
            $data['layers'] ?? [],
            fn (array $layer) => ($layer['nm'] ?? '') !== 'Background' && ($layer['ty'] ?? 0) !== 5,
        ));
        $data['nm'] = 'main';

        if (! is_dir(dirname($destination))) {
            mkdir(dirname($destination), 0755, true);
        }

        $zip = new ZipArchive;
        if ($zip->open($destination, ZipArchive::CREATE | ZipArchive::OVERWRITE) !== true) {
            return false;
        }
        $zip->addFromString('manifest.json', json_encode([
            'version' => '1.0',
            'animations' => [['id' => 'main', 'mode' => 'normal', 'direction' => 1]],
        ]));
        $zip->addFromString('animations/main.json', json_encode($data));

        return $zip->close();
    }

    /**
     * Whether two animation files hold the same animation: the same entries
     * for dotLottie archives, whose timestamps differ from one copy to the
     * next, the same bytes otherwise.
     */
    public static function sameContent(string $a, string $b): bool
    {
        if (! is_file($a) || ! is_file($b)) {
            return false;
        }

        $entriesA = self::entries($a);
        $entriesB = self::entries($b);
        if ($entriesA !== null || $entriesB !== null) {
            return $entriesA === $entriesB;
        }

        return file_get_contents($a) === file_get_contents($b);
    }

    /**
     * @return array<string, string>|null
     */
    private static function entries(string $path): ?array
    {
        $zip = new ZipArchive;
        if (! is_file($path) || $zip->open($path, ZipArchive::RDONLY) !== true) {
            return null;
        }

        $entries = [];
        for ($i = 0; $i < $zip->numFiles; $i++) {
            $name = $zip->getNameIndex($i);
            $entries[$name] = $zip->getFromIndex($i);
        }
        $zip->close();
        ksort($entries);

        return $entries;
    }
}
